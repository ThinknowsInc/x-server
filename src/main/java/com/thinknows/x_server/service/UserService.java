package com.thinknows.x_server.service;

import com.thinknows.x_server.model.User;
import com.thinknows.x_server.model.request.LoginRequest;
import com.thinknows.x_server.model.request.RefreshTokenRequest;
import com.thinknows.x_server.model.request.RegisterRequest;
import com.thinknows.x_server.model.response.TokenResponse;
import com.thinknows.x_server.model.response.UserProfileResponse;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {
    private final Map<String, User> userMap = new HashMap<>();
    private final Map<String, String> refreshTokenStore = new HashMap<>(); // username -> refreshToken
    private final Map<String, String> accessTokenStore = new HashMap<>(); // username -> accessToken
    private final Map<String, LocalDateTime> accessTokenExpiryStore = new HashMap<>(); // accessToken -> expiry
    private final Map<String, LocalDateTime> refreshTokenExpiryStore = new HashMap<>(); // refreshToken -> expiry
    
    // Token validity periods (in minutes)
    private static final int ACCESS_TOKEN_VALIDITY_MINUTES = 30; // 30 minutes
    private static final int REFRESH_TOKEN_VALIDITY_MINUTES = 43200; // 30 days
    
    private final AtomicLong idCounter = new AtomicLong(1);

    public User register(RegisterRequest request) {
        // Check if username already exists
        if (userMap.containsKey(request.getUsername())) {
            return null; // Username already exists
        }

        // Create new user
        User user = new User(
                request.getUsername(),
                request.getPassword(), // In a real app, you should hash the password
                request.getEmail(),
                request.getPhone()
        );
        user.setId(idCounter.getAndIncrement());

        // Save user to map
        userMap.put(user.getUsername(), user);
        return user;
    }

    public User login(LoginRequest request) {
        // Check if user exists
        User user = userMap.get(request.getUsername());
        if (user == null) {
            return null; // User not found
        }

        // Check if password matches
        String storedPassword = user.getPassword();
        if (storedPassword == null || !storedPassword.equals(request.getPassword())) {
            return null; // Password doesn't match or is null
        }

        return user;
    }

    public TokenResponse generateTokens(User user) {
        String username = user.getUsername();
        
        // Generate new tokens
        String accessToken = generateSecureToken();
        String refreshToken = generateSecureToken();
        
        // Set expiry times
        LocalDateTime accessTokenExpiry = LocalDateTime.now().plusMinutes(ACCESS_TOKEN_VALIDITY_MINUTES);
        LocalDateTime refreshTokenExpiry = LocalDateTime.now().plusMinutes(REFRESH_TOKEN_VALIDITY_MINUTES);
        
        // Store tokens
        accessTokenStore.put(username, accessToken);
        refreshTokenStore.put(username, refreshToken);
        accessTokenExpiryStore.put(accessToken, accessTokenExpiry);
        refreshTokenExpiryStore.put(refreshToken, refreshTokenExpiry);
        
        return new TokenResponse(accessToken, refreshToken, accessTokenExpiry, refreshTokenExpiry);
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
        User user = userMap.get(username);
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
        
        return userMap.get(username);
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
