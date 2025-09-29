package com.accountservice.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.accountservice.dto.TransactionalPasswordRequest;
import com.accountservice.model.Account;

@Service
public class TransactionalPasswordService {
    
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    
    public TransactionalPasswordService(AccountService accountService, PasswordEncoder passwordEncoder) {
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
    }
    
    // ✅ DEFINIR SENHA TRANSACIONAL
    @Transactional
    public void setTransactionalPassword(Long userId, TransactionalPasswordRequest request) {
        Account account = getPrimaryAccount(userId);
        
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Senha e confirmação não coincidem");
        }
        
        if (account.isTransactionalPasswordSet()) {
            throw new RuntimeException("Senha transacional já está configurada");
        }
        
        account.setTransactionalPassword(request.getPassword(), passwordEncoder);
        accountService.updateAccount(account);
        
        System.out.println("✅ Senha transacional configurada para conta: " + account.getAccountNumber());
    }
    
    // ✅ ALTERAR SENHA TRANSACIONAL
    @Transactional
    public void changeTransactionalPassword(Long userId, String currentPassword, String newPassword) {
        Account account = getPrimaryAccount(userId);
        
        // Valida senha atual
        account.validateTransactionalPassword(currentPassword, passwordEncoder);
        
        // Define nova senha
        account.setTransactionalPassword(newPassword, passwordEncoder);
        accountService.updateAccount(account);
        
        System.out.println("✅ Senha transacional alterada para conta: " + account.getAccountNumber());
    }
    
    // ✅ VALIDAR SENHA PARA TRANSAÇÃO
    @Transactional
    public boolean authorizeTransaction(Long userId, String transactionalPassword) {
        Account account = getPrimaryAccount(userId);
        
        try {
            return account.validateTransactionalPassword(transactionalPassword, passwordEncoder);
        } catch (Exception e) {
            accountService.updateAccount(account); // Salva tentativas falhas
            throw e;
        }
    }
    
    // ✅ VERIFICAR STATUS DA SENHA
    public String getPasswordStatus(Long userId) {
        Account account = getPrimaryAccount(userId);
        
        if (!account.isTransactionalPasswordSet()) {
            return "NOT_SET";
        }
        
        if (account.getPasswordBlockedUntil() != null && 
            LocalDateTime.now().isBefore(account.getPasswordBlockedUntil())) {
            return "BLOCKED";
        }
        
        return "ACTIVE";
    }
    
    private Account getPrimaryAccount(Long userId) {
        return accountService.getUserAccounts(userId).stream()
            .filter(acc -> acc.getStatus() == Account.AccountStatus.ACTIVE)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Usuário não possui conta ativa"));
    }
}