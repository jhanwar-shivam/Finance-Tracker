package com.finance.tracker.controller;

import com.finance.tracker.dto.SummaryDTO;
import com.finance.tracker.dto.TransactionDTO;
import com.finance.tracker.model.Transaction;
import com.finance.tracker.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    @Autowired
    TransactionService transactionService;

    @PostMapping("/add")
    public String addTransaction(@Valid @RequestBody Transaction transaction) {
        transactionService.addTransaction(transaction);
        return "Transaction added successfully";
    }

    @GetMapping("")
    public List<TransactionDTO> getTransactions() {
        return transactionService.getTransactions();
    }

    @GetMapping("/{category}")
    public List<TransactionDTO> getTransactionsByCategory(@PathVariable String category) {
        return transactionService.getTransactionsByCategory(category);
    }

    @DeleteMapping("delete/{id}")
    public String deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return "Transaction deleted successfully";
    }

    @PutMapping("/edit")
    public String editTransaction(@Valid @RequestBody Transaction transaction) {
        transactionService.editTransaction(transaction);
        return "Transaction edited successfully";
    }

    @GetMapping("/summary")
    public SummaryDTO getSummary() {
        return transactionService.getSummary();
    }
}
