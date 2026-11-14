package com.snipit.snipit.dto;

import java.time.LocalDateTime;

public record UrlResponse(
        Long id,
        String originalUrl,
        String shortCode,
        String shortUrl,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        long clickCount,
        Long maxClicks,
        boolean active,
        boolean passwordProtected) {
}