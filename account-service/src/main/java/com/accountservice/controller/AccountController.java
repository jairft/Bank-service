package com.accountservice.controller;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.accountservice.dto.TransactionRequest;
import com.accountservice.dto.TransactionResponse;
import com.accountservice.model.Account;
import com.accountservice.model.Transaction;
import com.accountservice.service.AccountService;
import com.accountservice.service.TransactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    
    private static final Logger log = LoggerFactory.getLogger(AccountController.class);
    private final AccountService accountService;
    private final TransactionService transactionService;
    
    public AccountController(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }
    
    // DEPÓSITO - Versão melhorada
    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable String accountNumber,
            @Valid @RequestBody TransactionRequest request) {
        
        log.info("Depositando R$ {:.2f} na conta: {}", request.getAmount(), accountNumber);
        TransactionResponse response = transactionService.deposit(accountNumber, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable String accountNumber,
            @Valid @RequestBody TransactionRequest request,
            @RequestParam String transactionalPassword) { // ✅ NOVO PARÂMETRO
        
        System.out.println("Sacando R$ " + request.getAmount() + " da conta: " + accountNumber);
        TransactionResponse response = transactionService.withdraw(accountNumber, request, transactionalPassword);
        return ResponseEntity.ok(response);
}
    
   
    @GetMapping("/{accountNumber}/transactions")
    public ResponseEntity<List<Transaction>> getAccountTransactions(@PathVariable String accountNumber) {
        log.info("Buscando extrato da conta: {}", accountNumber);
        
        Account account = accountService.getAccountByNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Conta não encontrada"));
        
        List<Transaction> transactions = transactionService.getAccountTransactions(account.getId());
        return ResponseEntity.ok(transactions);
    }
    
    // SALDO - Mantém o existente
    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String accountNumber) {
        log.info("Consultando saldo da conta: {}", accountNumber);
        BigDecimal balance = accountService.getBalance(accountNumber);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/{accountNumber}/details")
    public ResponseEntity<Map<String, String>> getAccountDetails(@PathVariable String accountNumber) {
        Account account = accountService.getAccountByNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Conta não encontrada"));
        
        Map<String, String> details = new HashMap<>();
        details.put("banco", account.getBankCode());
        details.put("agencia", account.getAgencyNumber());
        details.put("conta", account.getAccountNumber());
        details.put("titular", account.getUserName());
        details.put("cpf", account.getUserCpf());
        details.put("tipo", account.getType().toString());
        details.put("saldo", account.getBalance().toString());
        details.put("detalhesCompletos", account.getBankDetails());
        
        return ResponseEntity.ok(details);
    }
    
    @GetMapping("/user/{userId}/bank-info")
    public ResponseEntity<List<Map<String, String>>> getUserBankAccounts(@PathVariable Long userId) {
        List<Account> accounts = accountService.getUserAccounts(userId);
        
        List<Map<String, String>> bankAccounts = accounts.stream()
            .map(account -> {
                Map<String, String> info = new HashMap<>();
                info.put("agencia", account.getAgencyNumber());
                info.put("conta", account.getAccountNumber());
                info.put("tipo", account.getType().toString());
                info.put("saldo", String.format("R$ %.2f", account.getBalance()));
                info.put("detalhes", account.getFullAccountNumber());
                return info;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(bankAccounts);
    }
}