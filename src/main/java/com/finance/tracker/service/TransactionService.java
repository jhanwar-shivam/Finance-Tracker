package com.finance.tracker.service;

import com.finance.tracker.dao.TransactionDao;
import com.finance.tracker.dao.UserDao;
import com.finance.tracker.dto.SummaryDTO;
import com.finance.tracker.dto.TransactionDTO;
import com.finance.tracker.exception.ResourceNotFoundException;
import com.finance.tracker.exception.UserNotAuthenticatedException;
import com.finance.tracker.exception.UserNotFoundException;
import com.finance.tracker.model.CustomUserDetails;
import com.finance.tracker.model.Transaction;
import com.finance.tracker.model.TransactionType;
import com.finance.tracker.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    @Autowired
    TransactionDao transactionDao;

    @Autowired
    UserDao userDao;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void addTransaction(Transaction transaction) {
        User user = getCurrentLoggedInUser();
        transaction.setUser(user);
        transactionDao.save(transaction);
    }

    public List<TransactionDTO> getTransactions() {
        User user = getCurrentLoggedInUser();
        List<Transaction> transactions = transactionDao.findAllByUserOrderByDateDesc(user);
        return mapToDTOList(transactions);
    }

    public List<TransactionDTO> getTransactionsByCategory(String category) {
        User user = getCurrentLoggedInUser();
        List<Transaction> transactions = transactionDao.findAllByUserAndCategory(user, category);
        return mapToDTOList(transactions);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteTransaction(Long id) {
        User user = getCurrentLoggedInUser();
        transactionDao.deleteByUserAndTransactionId(user, id);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void editTransaction(Transaction transaction) {
        User currentUser = getCurrentLoggedInUser();

        Transaction existingTransaction = transactionDao.findByTransactionIdAndUser(transaction.getTransactionId(), currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found for the authenticated user"));

        existingTransaction.setTransactionType(transaction.getTransactionType());
        existingTransaction.setAmount(transaction.getAmount());
        existingTransaction.setCategory(transaction.getCategory());
        existingTransaction.setDescription(transaction.getDescription());
        existingTransaction.setDate(transaction.getDate());

        transactionDao.save(existingTransaction);
    }

    public SummaryDTO getSummary() {
        User user = getCurrentLoggedInUser();
        BigDecimal totalIncome = transactionDao.sumByUserAndType(user, TransactionType.INCOME).orElse(BigDecimal.ZERO);
        BigDecimal totalExpense = transactionDao.sumByUserAndType(user, TransactionType.EXPENSE).orElse(BigDecimal.ZERO);
        BigDecimal totalBalance = totalIncome.subtract(totalExpense);

        return new SummaryDTO(totalIncome, totalExpense, totalBalance);
    }

    private List<TransactionDTO> mapToDTOList(List<Transaction> transactions) {
        List<TransactionDTO> transactionDTOS = new ArrayList<>();
        for (Transaction transaction : transactions) {
            transactionDTOS.add(new TransactionDTO(
                    transaction.getTransactionId(),
                    transaction.getTransactionType(),
                    transaction.getAmount(),
                    transaction.getCategory(),
                    transaction.getDescription(),
                    transaction.getDate(),
                    transaction.getUser().getUserId()));
        }
        return transactionDTOS;
    }

    private User getCurrentLoggedInUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            String email = ((CustomUserDetails) principal).getUsername();
            return userDao.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("No user found by this email"));
        }

        throw new UserNotAuthenticatedException("User isn't authenticated!");
    }
}
