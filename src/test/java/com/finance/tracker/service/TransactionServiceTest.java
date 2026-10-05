package com.finance.tracker.service;

import com.finance.tracker.dao.TransactionDao;
import com.finance.tracker.dao.UserDao;
import com.finance.tracker.dto.SummaryDTO;
import com.finance.tracker.dto.TransactionDTO;
import com.finance.tracker.exception.ResourceNotFoundException;
import com.finance.tracker.model.CustomUserDetails;
import com.finance.tracker.model.Transaction;
import com.finance.tracker.model.TransactionType;
import com.finance.tracker.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionDao transactionDao;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private TransactionService transactionService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setUserName("Test User");
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("hashedPassword");

        CustomUserDetails userDetails = new CustomUserDetails(mockUser);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        lenient().when(userDao.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addTransaction_SetsUserAndPersists() {
        Transaction tx = new Transaction();
        tx.setAmount(new BigDecimal("100.00"));
        tx.setCategory("Food");
        tx.setTransactionType(TransactionType.EXPENSE);
        tx.setDescription("Groceries");
        tx.setDate(LocalDate.now());

        transactionService.addTransaction(tx);

        assertEquals(mockUser, tx.getUser());
        verify(transactionDao).save(tx);
    }

    @Test
    void getTransactions_LeveragesChronologicalIndexAndReturnsDTOs() {
        Transaction tx = new Transaction();
        tx.setTransactionId(10L);
        tx.setUser(mockUser);
        tx.setAmount(new BigDecimal("50.00"));
        tx.setCategory("Transport");
        tx.setTransactionType(TransactionType.EXPENSE);
        tx.setDescription("Metro ticket");
        tx.setDate(LocalDate.now());

        when(transactionDao.findAllByUserOrderByDateDesc(mockUser)).thenReturn(List.of(tx));

        List<TransactionDTO> dtos = transactionService.getTransactions();

        assertEquals(1, dtos.size());
        assertEquals(10L, dtos.get(0).transactionId());
        assertEquals("Transport", dtos.get(0).category());
        assertEquals(1L, dtos.get(0).userId());
    }

    @Test
    void getTransactionsByCategory_ReturnsMappedDTOs() {
        Transaction tx = new Transaction();
        tx.setTransactionId(11L);
        tx.setUser(mockUser);
        tx.setAmount(new BigDecimal("200.00"));
        tx.setCategory("Shopping");
        tx.setTransactionType(TransactionType.EXPENSE);
        tx.setDescription("Shoes");
        tx.setDate(LocalDate.now());

        when(transactionDao.findAllByUserAndCategory(mockUser, "Shopping")).thenReturn(List.of(tx));

        List<TransactionDTO> dtos = transactionService.getTransactionsByCategory("Shopping");

        assertEquals(1, dtos.size());
        assertEquals("Shopping", dtos.get(0).category());
    }

    @Test
    void editTransaction_SuccessWithStrictTenantIsolation() {
        Transaction existing = new Transaction();
        existing.setTransactionId(20L);
        existing.setUser(mockUser);
        existing.setAmount(new BigDecimal("30.00"));
        existing.setCategory("Food");
        existing.setTransactionType(TransactionType.EXPENSE);
        existing.setDescription("Snack");
        existing.setDate(LocalDate.now());
        existing.setVersion(1L);

        when(transactionDao.findByTransactionIdAndUser(20L, mockUser)).thenReturn(Optional.of(existing));

        Transaction updateRequest = new Transaction();
        updateRequest.setTransactionId(20L);
        updateRequest.setAmount(new BigDecimal("45.00"));
        updateRequest.setCategory("Dining");
        updateRequest.setTransactionType(TransactionType.EXPENSE);
        updateRequest.setDescription("Dinner");
        updateRequest.setDate(LocalDate.now());

        transactionService.editTransaction(updateRequest);

        assertEquals(new BigDecimal("45.00"), existing.getAmount());
        assertEquals("Dining", existing.getCategory());
        verify(transactionDao).save(existing);
    }

    @Test
    void editTransaction_ThrowsResourceNotFoundWhenNotOwnedByTenant() {
        when(transactionDao.findByTransactionIdAndUser(99L, mockUser)).thenReturn(Optional.empty());

        Transaction updateRequest = new Transaction();
        updateRequest.setTransactionId(99L);

        assertThrows(ResourceNotFoundException.class, () -> transactionService.editTransaction(updateRequest));
        verify(transactionDao, never()).save(any());
    }

    @Test
    void editTransaction_ThrowsOptimisticLockExceptionOnConcurrentUpdate() {
        Transaction existing = new Transaction();
        existing.setTransactionId(20L);
        existing.setUser(mockUser);
        existing.setVersion(1L);

        when(transactionDao.findByTransactionIdAndUser(20L, mockUser)).thenReturn(Optional.of(existing));
        when(transactionDao.save(existing)).thenThrow(
                new ObjectOptimisticLockingFailureException(Transaction.class, 20L));

        Transaction updateRequest = new Transaction();
        updateRequest.setTransactionId(20L);
        updateRequest.setAmount(new BigDecimal("50.00"));
        updateRequest.setCategory("Food");
        updateRequest.setTransactionType(TransactionType.EXPENSE);
        updateRequest.setDescription("Dinner");
        updateRequest.setDate(LocalDate.now());

        assertThrows(ObjectOptimisticLockingFailureException.class, () -> transactionService.editTransaction(updateRequest));
    }

    @Test
    void deleteTransaction_EnforcesTenantScopedDeletion() {
        transactionService.deleteTransaction(30L);
        verify(transactionDao).deleteByUserAndTransactionId(mockUser, 30L);
    }

    @Test
    void getSummary_ComputesBalanceCorrectly() {
        when(transactionDao.sumByUserAndType(mockUser, TransactionType.INCOME))
                .thenReturn(Optional.of(new BigDecimal("5000.00")));
        when(transactionDao.sumByUserAndType(mockUser, TransactionType.EXPENSE))
                .thenReturn(Optional.of(new BigDecimal("1500.00")));

        SummaryDTO summary = transactionService.getSummary();

        assertEquals(new BigDecimal("5000.00"), summary.totalIncome());
        assertEquals(new BigDecimal("1500.00"), summary.totalExpense());
        assertEquals(new BigDecimal("3500.00"), summary.totalBalance());
    }
}
