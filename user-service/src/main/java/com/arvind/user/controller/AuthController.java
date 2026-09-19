package com.arvind.user.controller;

import com.arvind.user.dto.AuthResponse;
import com.arvind.user.dto.LoginRequest;
import com.arvind.user.dto.RegisterRequest;
import com.arvind.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.arvind.user.dto.LogoutRequest;

import org.springframework.web.bind.annotation.PathVariable;
import com.arvind.user.dto.UserResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {

        authService.register(request);

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestBody LogoutRequest request) {

        authService.logout(request);

        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable String id) {

        return ResponseEntity.ok(
                authService.getUserById(id)
        );
    }
}