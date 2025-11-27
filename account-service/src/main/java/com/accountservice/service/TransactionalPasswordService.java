package com.accountservice.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.accountservice.dto.ResetTransactionalPasswordRequest;
import com.accountservice.dto.TransactionalPasswordRequest;
import com.accountservice.dto.UpdateTransactionalPasswordRequest;
import com.accountservice.exception.AccountNotFoundException;
import com.accountservice.exception.InvalidTransactionalPasswordException;
import com.accountservice.exception.PasswordAlreadySetException;
import com.accountservice.exception.PasswordMismatchException;
import com.accountservice.exception.TokenExpiredException;
import com.accountservice.model.Account;
import com.accountservice.repository.AccountRepository;

@Service
public class TransactionalPasswordService {
    private static final Logger log = LoggerFactory.getLogger(TransactionalPasswordService.class);
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    
    public TransactionalPasswordService(AccountService accountService, PasswordEncoder passwordEncoder, AccountRepository accountRepository) {
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
    }
    
    // ================================
    // DEFINIR SENHA TRANSACIONAL
    // ================================
    @Transactional
    public void setTransactionalPassword(Long userId, TransactionalPasswordRequest request) {
        Account account = getPrimaryAccount(userId);
           
        // Valida senha x confirmação
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Senha transacional e confirmação não coincidem");
        }
        
        // Verifica se já foi definida
        if (account.isTransactionalPasswordSet()) {
            throw new PasswordAlreadySetException("Senha transacional já está configurada");
        }
        
        // Define a senha usando passwordEncoder
        account.setTransactionalPassword(request.getPassword(), passwordEncoder);
        
        
        accountService.updateAccount(account);
        
        System.out.println("✅ Senha transacional configurada para conta: " + account.getAccountNumber());
    }
    
    // ================================
    // ALTERAR SENHA TRANSACIONAL
    // ================================
    @Transactional
    public void changeTransactionalPassword(Long userId, UpdateTransactionalPasswordRequest request) {
        Account account = getPrimaryAccount(userId);

        // Valida senha atual
        account.validateTransactionalPassword(request.getCurrentPassword(), passwordEncoder);

        // Verifica se nova senha e confirmação são iguais
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Nova senha e confirmação não coincidem");
        }

        // Verifica se nova senha é diferente da atual
        if (passwordEncoder.matches(request.getNewPassword(), account.getTransactionalPassword())) {
            throw new PasswordMismatchException("A nova senha não pode ser igual à senha atual");
        }

        // Define nova senha
        account.setTransactionalPassword(request.getNewPassword(), passwordEncoder);
        accountService.updateAccount(account);

        System.out.println("✅ Senha transacional alterada para conta: " + account.getAccountNumber());
    }



     // ================================
    // AUTORIZAR TRANSAÇÃO
    // ================================
    
    public boolean authorizeTransaction(Long userId, String transactionalPassword) {
    Account account = getPrimaryAccount(userId);

    if (!account.validateTransactionalPassword(transactionalPassword, passwordEncoder)) {
        throw new InvalidTransactionalPasswordException("Senha transacional inválida");
    }

    accountService.updateAccount(account); // Salva tentativas falhas
    return true;
}

    
    // ================================
    // STATUS DA SENHA
    // ================================
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
    
    // ================================
    // UTILITÁRIO
    // ================================
    private Account getPrimaryAccount(Long userId) {
        return accountService.getUserAccounts(userId).stream()
            .filter(acc -> acc.getStatus() == Account.AccountStatus.ACTIVE)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Usuário não possui conta ativa"));
    }
    

     public String resendActivationTokenByUserId(Long userId) {
        Account account = accountRepository.findFirstByUserId(userId)
                .orElseThrow(() -> new AccountNotFoundException("Conta do usuário não encontrada"));

        String newToken = generateActivationToken();
        account.setActivationToken(newToken);
        account.setActivationExpires(LocalDateTime.now().plusHours(24));
        accountRepository.save(account);

        log.info("Token para reset de senha transacional gerado para: {}", account.getUserEmail());
        log.info("✅ TOKEN DE RESET: {}", newToken);

        return newToken;
    }

    
    private String generateActivationToken() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 25);
    }

    public void resetTransactionalPasswordByUserId(Long userId, ResetTransactionalPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("As senhas não coincidem.");
        }

        Account account = accountRepository.findFirstByUserId(userId)
                .orElseThrow(() -> new AccountNotFoundException("Conta do usuário não encontrada"));

        if (!request.getToken().equals(account.getActivationToken())) {
            throw new TokenExpiredException("Token inválido.");
        }

        if (account.getActivationExpires() == null || account.getActivationExpires().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("O token expirou. Solicite um novo.");
        }

        account.setTransactionalPassword(passwordEncoder.encode(request.getNewPassword()));
        account.setActivationToken(null);
        account.setActivationExpires(null);
        accountRepository.save(account);

        log.info("Senha transacional redefinida para o usuário: {}", account.getUserEmail());
    }

}
