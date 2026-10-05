package com.finance.tracker.dao;

import com.finance.tracker.model.Transaction;
import com.finance.tracker.model.TransactionType;
import com.finance.tracker.model.User;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionDao extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByUser(User user);

    List<Transaction> findAllByUserOrderByDateDesc(User user);

    List<Transaction> findAllByUserAndCategory(User user, String category);

    Optional<Transaction> findByTransactionIdAndUser(Long transactionId, User user);

    @Query("SELECT sum(t.amount) FROM Transaction t WHERE t.user = :user AND t.transactionType = :type")
    Optional<BigDecimal> sumByUserAndType(@Param("user") User user, @Param("type") TransactionType type);

    @Transactional
    void deleteByUserAndTransactionId(User user, Long transactionId);
}
