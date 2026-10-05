package com.finance.tracker.dto;

import java.math.BigDecimal;

public record SummaryDTO(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal totalBalance) {
}
