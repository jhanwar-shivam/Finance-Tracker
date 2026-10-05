package com.finance.tracker.dto;

import com.finance.tracker.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionDTO(
        Long transactionId,
        TransactionType transactionType,
        BigDecimal amount,
        String category,
        String description,
        LocalDate date,
        Long userId) {
}
