package com.thinknows.x_server.controller;

import com.thinknows.x_server.model.response.ApiResponse;
import com.thinknows.x_server.model.response.UserProfileResponse;
import com.thinknows.x_server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(@PathVariable Long userId) {
        UserProfileResponse profile = userService.generateRandomUserProfile(userId);
        return ResponseEntity.ok(new ApiResponse<>(200, "User profile retrieved successfully", profile));
    }
}
