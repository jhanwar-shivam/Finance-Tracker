package com.finance.tracker.dto;

public record ErrorDTO(
        int statusCode,
        String message
) {
}
