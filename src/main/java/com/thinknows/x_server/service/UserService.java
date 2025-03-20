package com.thinknows.x_server.service;

import com.thinknows.x_server.model.DeviceInfo;
import com.thinknows.x_server.model.User;
import com.thinknows.x_server.model.request.LoginRequest;
import com.thinknows.x_server.model.request.RefreshTokenRequest;
import com.thinknows.x_server.model.request.RegisterRequest;
import com.thinknows.x_server.model.request.TwoFactorVerifyRequest;
import com.thinknows.x_server.model.response.DeviceSession;
import com.thinknows.x_server.model.response.LoginResponse;
import com.thinknows.x_server.model.response.TokenResponse;
import com.thinknows.x_server.model.response.UserProfileResponse;
import com.thinknows.x_server.repository.UserRepository;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    /**
     * 账户锁定异常
     */
    public static class AccountLockedException extends RuntimeException {
        public AccountLockedException(String message) {
            super(message);
        }
    }
    @Autowired
    private UserRepository userRepository;
    private final Map<String, String> refreshTokenStore = new HashMap<>(); // username -> refreshToken
    private final Map<String, String> accessTokenStore = new HashMap<>(); // username -> accessToken
    private final Map<String, LocalDateTime> accessTokenExpiryStore = new HashMap<>(); // accessToken -> expiry
    private final Map<String, LocalDateTime> refreshTokenExpiryStore = new HashMap<>(); // refreshToken -> expiry
    
    // 存储用户的活跃会话
    private final Map<String, Set<String>> userActiveSessions = new HashMap<>(); // username -> Set<sessionId>
    private final Map<String, String> sessionToUsername = new HashMap<>(); // sessionId -> username
    private final Map<String, DeviceSession> sessionInfo = new HashMap<>(); // sessionId -> DeviceSession
    
    // 存储登录失败尝试
    private final Map<String, Integer> failedLoginAttempts = new HashMap<>(); // username -> attempts count
    private final Map<String, LocalDateTime> lockoutTime = new HashMap<>(); // username -> lockout until
    
    // 存储二次验证码
    private final Map<String, String> twoFactorCodes = new HashMap<>(); // username -> code
    private final Map<String, LocalDateTime> twoFactorCodeExpiry = new HashMap<>(); // username -> expiry
    private final Map<String, String> twoFactorTokens = new HashMap<>(); // token -> username
    
    // Token validity periods (in minutes)
    private static final int ACCESS_TOKEN_VALIDITY_MINUTES = 30; // 30 minutes
    private static final int REFRESH_TOKEN_VALIDITY_MINUTES = 43200; // 30 days
    private static final int EXTENDED_REFRESH_TOKEN_VALIDITY_DAYS = 30; // 30 days for "remember me"
    
    // 登录尝试限制
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;
    
    // 二次验证码有效期
    private static final int TWO_FACTOR_CODE_VALIDITY_MINUTES = 10;
    
    // JWT 密钥
    private static final Key JWT_SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    
    // 不再需要手动管理ID，由数据库自动生成

    public User register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            return null; // Username already exists
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return null; // Email already exists
        }

        // Hash the password using BCrypt
        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());
        
        // Create new user with hashed password
        User user = new User(
                request.getUsername(),
                hashedPassword,
                request.getEmail(),
                request.getPhone()
        );

        // Save user to database
        return userRepository.save(user);
    }

    
    /**
     * 登录方法，支持密码验证、登录尝试限制和二次验证
     */
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        
        // 检查是否被锁定
        if (isUserLockedOut(username)) {
            throw new AccountLockedException("账户已被锁定，请稍后再试");
        }
        
        // 检查用户是否存在
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            recordFailedLoginAttempt(username);
            return null; // 用户不存在
        }
        
        // 检查密码是否匹配（使用 BCrypt 验证）
        String storedPassword = user.getPassword();
        if (storedPassword == null || !BCrypt.checkpw(request.getPassword(), storedPassword)) {
            recordFailedLoginAttempt(username);
            return null; // 密码不匹配
        }
        
        // 登录成功，重置失败计数
        resetFailedLoginAttempts(username);
        
        // 检查用户是否启用了二次验证
        if (user.isTwoFactorEnabled()) {
            // 生成二次验证码
            String twoFactorCode = generateTwoFactorCode();
            String twoFactorToken = generateTwoFactorToken(username);
            
            // 存储验证码
            twoFactorCodes.put(username, twoFactorCode);
            twoFactorCodeExpiry.put(username, LocalDateTime.now().plusMinutes(TWO_FACTOR_CODE_VALIDITY_MINUTES));
            twoFactorTokens.put(twoFactorToken, username);
            
            // 发送验证码到用户邮箱或手机（模拟）
            sendTwoFactorCode(user, twoFactorCode);
            
            // 返回需要二次验证的响应
            LoginResponse response = new LoginResponse();
            response.setUser(cleanUserForResponse(user));
            response.setRequiresTwoFactor(true);
            response.setTwoFactorToken(twoFactorToken);
            return response;
        }
        
        // 不需要二次验证，直接生成令牌
        TokenResponse tokens = generateTokens(user, request.getDeviceInfo(), request.isRememberMe());
        
        // 获取用户的活跃会话
        List<DeviceSession> activeSessions = getUserActiveSessions(username);
        
        return new LoginResponse(tokens, cleanUserForResponse(user), false, null, activeSessions);
    }
    
    /**
     * 清理敏感信息，返回安全的用户对象
     */
    private User cleanUserForResponse(User user) {
        User cleanUser = new User();
        cleanUser.setId(user.getId());
        cleanUser.setUsername(user.getUsername());
        cleanUser.setEmail(user.getEmail());
        cleanUser.setPhone(user.getPhone());
        cleanUser.setCreatedAt(user.getCreatedAt());
        cleanUser.setUpdatedAt(user.getUpdatedAt());
        cleanUser.setActive(user.isActive());
        cleanUser.setTwoFactorEnabled(user.isTwoFactorEnabled());
        return cleanUser;
    }
    
    /**
     * 记录登录失败尝试
     */
    private void recordFailedLoginAttempt(String username) {
        int attempts = failedLoginAttempts.getOrDefault(username, 0) + 1;
        failedLoginAttempts.put(username, attempts);
        
        // 如果达到最大尝试次数，锁定账户
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            lockoutTime.put(username, LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
        }
    }
    
    /**
     * 检查用户是否被锁定
     */
    private boolean isUserLockedOut(String username) {
        LocalDateTime lockedUntil = lockoutTime.get(username);
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }
    
    /**
     * 重置登录失败计数
     */
    private void resetFailedLoginAttempts(String username) {
        failedLoginAttempts.remove(username);
        lockoutTime.remove(username);
    }
    
    /**
     * 生成二次验证码
     */
    private String generateTwoFactorCode() {
        // 生成 6 位数字验证码
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
    
    /**
     * 生成二次验证令牌
     */
    private String generateTwoFactorToken(String username) {
        return UUID.randomUUID().toString();
    }
    
    /**
     * 验证二次验证码
     */
    public LoginResponse verifyTwoFactorCode(TwoFactorVerifyRequest request) {
        // 验证令牌
        String token = request.getTwoFactorToken();
        if (token == null || !twoFactorTokens.containsKey(token)) {
            return null; // 无效的令牌
        }
        
        // 获取用户名
        String username = twoFactorTokens.get(token);
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return null; // 用户不存在
        }
        
        // 检查验证码
        String storedCode = twoFactorCodes.get(username);
        LocalDateTime expiry = twoFactorCodeExpiry.get(username);
        
        if (storedCode == null || expiry == null || expiry.isBefore(LocalDateTime.now())) {
            return null; // 验证码不存在或已过期
        }
        
        if (!storedCode.equals(request.getCode())) {
            return null; // 验证码不匹配
        }
        
        // 验证成功，清除验证码
        twoFactorCodes.remove(username);
        twoFactorCodeExpiry.remove(username);
        twoFactorTokens.remove(token);
        
        // 生成令牌
        TokenResponse tokens = generateTokens(user, request.getDeviceInfo(), false);
        
        // 获取用户的活跃会话
        List<DeviceSession> activeSessions = getUserActiveSessions(username);
        
        return new LoginResponse(tokens, cleanUserForResponse(user), false, null, activeSessions);
    }
    
    /**
     * 模拟发送二次验证码
     */
    private void sendTwoFactorCode(User user, String code) {
        // 在实际应用中，这里应该发送邮件或短信
        System.out.println("Sending 2FA code to " + user.getEmail() + ": " + code);
    }

    /**
     * 获取用户的活跃会话
     */
    public List<DeviceSession> getUserActiveSessions(String username) {
        Set<String> sessionIds = userActiveSessions.getOrDefault(username, new HashSet<>());
        return sessionIds.stream()
                .map(sessionInfo::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 生成访问令牌和刷新令牌
     */
    public TokenResponse generateTokens(User user) {
        return generateTokens(user, null, false);
    }
    
    /**
     * 生成访问令牌和刷新令牌，支持设备信息和记住我功能
     */
    public TokenResponse generateTokens(User user, DeviceInfo deviceInfo, boolean rememberMe) {
        String username = user.getUsername();
        
        // 生成 JWT 令牌
        String accessToken = generateJwtToken(user, ACCESS_TOKEN_VALIDITY_MINUTES);
        String refreshToken = generateJwtToken(user, rememberMe ? 
                EXTENDED_REFRESH_TOKEN_VALIDITY_DAYS * 24 * 60 : REFRESH_TOKEN_VALIDITY_MINUTES);

        // 设置过期时间
        LocalDateTime accessTokenExpiry = LocalDateTime.now().plusMinutes(ACCESS_TOKEN_VALIDITY_MINUTES);
        LocalDateTime refreshTokenExpiry = rememberMe ?
                LocalDateTime.now().plusDays(EXTENDED_REFRESH_TOKEN_VALIDITY_DAYS) :
                LocalDateTime.now().plusMinutes(REFRESH_TOKEN_VALIDITY_MINUTES);
        
        // 存储令牌
        accessTokenStore.put(username, accessToken);
        refreshTokenStore.put(username, refreshToken);
        accessTokenExpiryStore.put(accessToken, accessTokenExpiry);
        refreshTokenExpiryStore.put(refreshToken, refreshTokenExpiry);
        
        // 创建新会话
        if (deviceInfo != null) {
            String sessionId = UUID.randomUUID().toString();
            DeviceSession session = new DeviceSession();
            session.setSessionId(sessionId);
            session.setDeviceInfo(deviceInfo);
            session.setLoginTime(LocalDateTime.now());
            session.setLastActivityTime(LocalDateTime.now());
            session.setIpAddress("127.0.0.1"); // 在实际应用中，从请求中获取
            session.setCurrentDevice(true);
            
            // 存储会话信息
            Set<String> userSessions = userActiveSessions.computeIfAbsent(username, k -> new HashSet<>());
            userSessions.add(sessionId);
            sessionToUsername.put(sessionId, username);
            sessionInfo.put(sessionId, session);
        }

        // 返回令牌响应
        return new TokenResponse(accessToken, refreshToken, ACCESS_TOKEN_VALIDITY_MINUTES, 
                rememberMe ? EXTENDED_REFRESH_TOKEN_VALIDITY_DAYS * 24 * 60 : REFRESH_TOKEN_VALIDITY_MINUTES);
    }
    
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        // Check if refresh token exists and is valid
        if (!refreshTokenExpiryStore.containsKey(refreshToken)) {
            return null; // Token not found
        }
        
        LocalDateTime expiry = refreshTokenExpiryStore.get(refreshToken);
        if (expiry.isBefore(LocalDateTime.now())) {
            // Remove expired token
            String username = getUsernameByRefreshToken(refreshToken);
            if (username != null) {
                refreshTokenStore.remove(username);
            }
            refreshTokenExpiryStore.remove(refreshToken);
            return null; // Token expired
        }
        
        // Get username from refresh token
        String username = getUsernameByRefreshToken(refreshToken);
        if (username == null) {
            return null; // Username not found
        }
        
        // Get user
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return null; // User not found
        }
        
        // Generate new access token
        String newAccessToken = generateSecureToken();
        LocalDateTime newAccessTokenExpiry = LocalDateTime.now().plusMinutes(ACCESS_TOKEN_VALIDITY_MINUTES);
        
        // Update access token
        accessTokenStore.put(username, newAccessToken);
        accessTokenExpiryStore.put(newAccessToken, newAccessTokenExpiry);
        
        // Return new tokens (keeping the same refresh token)
        return new TokenResponse(
            newAccessToken, 
            refreshToken, 
            newAccessTokenExpiry, 
            refreshTokenExpiryStore.get(refreshToken)
        );
    }
    
    public boolean validateAccessToken(String accessToken) {
        if (!accessTokenExpiryStore.containsKey(accessToken)) {
            return false; // Token not found
        }
        
        LocalDateTime expiry = accessTokenExpiryStore.get(accessToken);
        return !expiry.isBefore(LocalDateTime.now()); // Token is valid if not expired
    }
    
    public User getUserByAccessToken(String accessToken) {
        if (!validateAccessToken(accessToken)) {
            return null; // Invalid token
        }
        
        String username = getUsernameByAccessToken(accessToken);
        if (username == null) {
            return null; // Username not found
        }
        
        return userRepository.findByUsername(username).orElse(null);
    }
    
    private String getUsernameByAccessToken(String accessToken) {
        for (Map.Entry<String, String> entry : accessTokenStore.entrySet()) {
            if (entry.getValue().equals(accessToken)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    private String getUsernameByRefreshToken(String refreshToken) {
        for (Map.Entry<String, String> entry : refreshTokenStore.entrySet()) {
            if (entry.getValue().equals(refreshToken)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * 生成 JWT 令牌
     */
    private String generateJwtToken(User user, int validityMinutes) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validityMinutes * 60 * 1000);
        
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .signWith(JWT_SECRET_KEY)
                .compact();
    }
    
    private String generateSecureToken() {
        // In a real application, you would use a proper token generation mechanism like JWT
        // For simplicity, we'll create a secure random token
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32]; // 256 bits
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    public UserProfileResponse generateRandomUserProfile(Long userId) {
        // Generate a random user profile for demo purposes
        Random random = new Random();
        
        // Random data for the profile
        String[] firstNames = {"John", "Jane", "Michael", "Emily", "David", "Sarah", "Robert", "Lisa"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller", "Wilson"};
        String[] cities = {"New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia", "San Antonio", "San Diego"};
        String[] countries = {"USA", "Canada", "UK", "Australia", "Germany", "France", "Japan", "China"};
        String[] interests = {"Photography", "Cooking", "Traveling", "Reading", "Gaming", "Sports", "Music", "Art", 
                             "Technology", "Fashion", "Movies", "Hiking", "Yoga", "Dancing", "Writing", "Gardening"};
        
        // Generate random values
        String firstName = firstNames[random.nextInt(firstNames.length)];
        String lastName = lastNames[random.nextInt(lastNames.length)];
        String fullName = firstName + " " + lastName;
        String username = (firstName + lastName + random.nextInt(100)).toLowerCase();
        String email = username + "@example.com";
        String phone = "" + (1000000000 + random.nextInt(9000000));
        String location = cities[random.nextInt(cities.length)] + ", " + countries[random.nextInt(countries.length)];
        
        // Generate random bio
        String[] bioTemplates = {
            "Professional %s enthusiast with a passion for %s.",
            "%s lover and %s expert based in %s.",
            "Exploring the world of %s and %s. Based in %s.",
            "%s professional with %d+ years of experience in %s."
        };
        
        String bioTemplate = bioTemplates[random.nextInt(bioTemplates.length)];
        String interest1 = interests[random.nextInt(interests.length)];
        String interest2 = interests[random.nextInt(interests.length)];
        String bio;
        
        if (bioTemplate.contains("%d")) {
            bio = String.format(bioTemplate, interest1, 2 + random.nextInt(8), interest2);
        } else {
            bio = String.format(bioTemplate, interest1, interest2, location.split(",")[0]);
        }
        
        // Generate random interests (3-5 interests)
        int interestCount = 3 + random.nextInt(3);
        List<String> userInterests = Arrays.asList(interests).subList(0, interestCount);
        
        // Generate random join date (within the last 3 years)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime joinDate = now.minusDays(random.nextInt(365 * 3));
        
        // Generate random follower and following counts
        int followersCount = random.nextInt(10000);
        int followingCount = random.nextInt(1000);
        
        // Generate random avatar URL
        String avatar = "https://randomuser.me/api/portraits/" + 
                       (random.nextBoolean() ? "men/" : "women/") + 
                       random.nextInt(100) + ".jpg";
        
        // Generate random website (50% chance of having a website)
        String website = random.nextBoolean() ? "https://" + username + ".example.com" : null;
        
        // Generate random verified status (20% chance of being verified)
        boolean verified = random.nextInt(5) == 0;
        
        // Create and return the profile
        return new UserProfileResponse(
            userId,
            username,
            email,
            phone,
            fullName,
            avatar,
            bio,
            joinDate,
            followersCount,
            followingCount,
            userInterests,
            location,
            website,
            verified
        );
    }
}
