package com.snipit.snipit.controller;

import com.snipit.snipit.dto.LoginRequest;
import com.snipit.snipit.dto.LoginResponse;
import com.snipit.snipit.dto.RegisterRequest;
import com.snipit.snipit.dto.UserResponse;
import com.snipit.snipit.model.User;
import com.snipit.snipit.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        User user = authService.register(request.name(), request.email(), request.password());
        UserResponse body = new UserResponse(
                user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request.email(), request.password()));
    }
}