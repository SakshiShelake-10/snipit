package com.snipit.snipit.dto;

import java.time.LocalDateTime;

public record CreateUrlRequest(String originalUrl, String customAlias, String password,
                               LocalDateTime expiresAt, Long maxClicks) {
}