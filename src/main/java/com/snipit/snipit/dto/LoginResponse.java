package com.snipit.snipit.dto;

public record LoginResponse(String token, UserResponse user) {
}