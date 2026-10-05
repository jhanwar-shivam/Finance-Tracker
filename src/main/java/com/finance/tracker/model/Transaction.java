package com.finance.tracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
    name = "transactions",
    indexes = {
        @Index(name = "idx_tx_user_date", columnList = "user_id, date"),
        @Index(name = "idx_tx_user_category", columnList = "user_id, category"),
        @Index(name = "idx_tx_user_type", columnList = "user_id, transaction_type")
    }
)
@Data
@Valid
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotBlank
    @Column(nullable = false)
    private String category;

    @NotBlank
    @Column(nullable = false)
    private String description;

    @NotNull
    @PastOrPresent
    @Column(nullable = false)
    private LocalDate date;

    @Version
    @Column(name = "version")
    private Long version;

}
