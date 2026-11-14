package com.snipit.snipit.dto;

import java.time.LocalDateTime;

public record ErrorResponse(int status, String error, String message, LocalDateTime timestamp) {
}