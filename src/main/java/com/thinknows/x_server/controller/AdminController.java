package com.thinknows.x_server.controller;

import com.thinknows.x_server.model.User;
import com.thinknows.x_server.model.response.ApiResponse;
import com.thinknows.x_server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final UserRepository userRepository;

    @Autowired
    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        
        // 清除敏感信息
        List<User> cleanUsers = users.stream().map(user -> {
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
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("All users retrieved successfully", cleanUsers));
    }
}
