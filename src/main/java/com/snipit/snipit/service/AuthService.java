package com.snipit.snipit.service;

import com.snipit.snipit.dto.LoginResponse;
import com.snipit.snipit.dto.UserResponse;
import com.snipit.snipit.model.User;
import com.snipit.snipit.repository.UserRepository;
import com.snipit.snipit.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public User register(String name, String email, String password) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be empty");
        }
        if (email == null || !email.trim().matches(EMAIL_PATTERN)) {
            throw new IllegalArgumentException("Enter a valid email address");
        }
        if (password == null || password.length() < 8 || password.length() > 72) {
            throw new IllegalArgumentException("Password must be between 8 and 72 characters");
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User user = new User();
        user.setName(name.trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(password));

        return userRepository.save(user);
    }

    public LoginResponse login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Email and password are required");
        }

        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .filter(u -> passwordEncoder.matches(password, u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        String token = jwtService.generateToken(user);
        UserResponse userResponse = new UserResponse(
                user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
        return new LoginResponse(token, userResponse);
    }
}