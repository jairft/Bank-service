package com.accountservice.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.accountservice.dto.TransactionRequest;
import com.accountservice.dto.TransactionResponse;
import com.accountservice.exception.AccountNotFoundException;
import com.accountservice.exception.InsufficientBalanceException;
import com.accountservice.exception.InvalidTransactionalPasswordException;
import com.accountservice.model.Account;
import com.accountservice.model.Transaction;
import com.accountservice.repository.TransactionRepository;

@Service
public class TransactionService {
    
    // ✅ CORREÇÃO: Logger correto
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    private final TransactionalPasswordService passwordService;
    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    
    public TransactionService(AccountService accountService, TransactionRepository transactionRepository, TransactionalPasswordService passwordService) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
        this.passwordService = passwordService;
    }
    
    @Transactional
    public TransactionResponse deposit(String accountNumber, TransactionRequest request) {
        // Logging inicial
        System.out.println("Processando depósito de R$ " + request.getAmount() + " na conta: " + accountNumber);

        // Busca a conta, lançando exception customizada se não existir
        Account account = accountService.getAccountByNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada: " + accountNumber));

        BigDecimal previousBalance = account.getBalance();

        // Executa o depósito
        account.deposit(request.getAmount());
        Account updatedAccount = accountService.updateAccount(account);

        // Registra a transação
        Transaction transaction = createTransaction(
                updatedAccount,
                Transaction.TransactionType.DEPOSIT,
                request.getAmount(),
                request.getDescription(),
                Transaction.TransactionStatus.COMPLETED
        );

        System.out.println("Depósito realizado com sucesso. Novo saldo: R$ " + updatedAccount.getBalance());

        return new TransactionResponse(
                "DEPOSIT",
                accountNumber,
                request.getAmount(),
                previousBalance,
                updatedAccount.getBalance(),
                "Depósito realizado com sucesso"
        );
    }
    
    @Transactional
    public TransactionResponse withdraw(String accountNumber, TransactionRequest request, String transactionalPassword) {
        System.out.println("🔐 Validando senha transacional para saque...");

        // Busca conta ou lança exception customizada
        Account account = accountService.getAccountByNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada: " + accountNumber));

        System.out.println("Processando saque de R$ " + request.getAmount() + " da conta: " + accountNumber);

        // Valida senha transacional
        if (!passwordService.authorizeTransaction(account.getUserId(), transactionalPassword)) {
            throw new InvalidTransactionalPasswordException("Senha transacional inválida");
        }

        // Verifica saldo suficiente
        if (!account.hasSufficientBalance(request.getAmount())) {
            throw new InsufficientBalanceException(
                    "Saldo insuficiente. Saldo atual: R$ " + account.getBalance() + ", Valor solicitado: R$ " + request.getAmount()
            );
        }

        BigDecimal previousBalance = account.getBalance();

        // Executa o saque
        account.withdraw(request.getAmount());
        Account updatedAccount = accountService.updateAccount(account);

        // Registra a transação
        Transaction transaction = createTransaction(
                updatedAccount,
                Transaction.TransactionType.WITHDRAW,
                request.getAmount(),
                request.getDescription(),
                Transaction.TransactionStatus.COMPLETED
        );

        System.out.println("Saque realizado com sucesso. Novo saldo: R$ " + updatedAccount.getBalance());

        return new TransactionResponse(
                "WITHDRAW",
                accountNumber,
                request.getAmount(),
                previousBalance,
                updatedAccount.getBalance(),
                "Saque realizado com sucesso"
        );
    }
    
    private Transaction createTransaction(Account account, 
                                        Transaction.TransactionType type, 
                                        BigDecimal amount, 
                                        String description, 
                                        Transaction.TransactionStatus status) {
        
        Transaction transaction = new Transaction();
        transaction.setTransactionId(generateTransactionId());
        transaction.setAccountId(account.getId());
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        
        // Calcula saldo anterior corretamente
        BigDecimal previousBalance = type == Transaction.TransactionType.DEPOSIT 
            ? account.getBalance().subtract(amount) 
            : account.getBalance().add(amount);
            
        transaction.setPreviousBalance(previousBalance);
        transaction.setNewBalance(account.getBalance());
        transaction.setStatus(status);
        transaction.setCreatedAt(LocalDateTime.now());
        
        return transactionRepository.save(transaction);
    }
    
    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
    
    public List<Transaction> getAccountTransactions(Long accountId) {
        return transactionRepository.findByAccountId(accountId);
    }
}