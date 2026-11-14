package com.snipit.snipit.dto;

import java.time.LocalDateTime;

public record UpdateUrlRequest(String originalUrl, String password,
                               LocalDateTime expiresAt, Long maxClicks) {
}