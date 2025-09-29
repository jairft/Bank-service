package com.accountservice.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    // Dados do usuário
    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;
    
    @Column(name = "user_cpf", nullable = false, length = 11)
    private String userCpf;
    
    @Column(name = "user_email", nullable = false, length = 150)
    private String userEmail;
    
    @Column(name = "user_phone", length = 15)
    private String userPhone;
    
    // ✅ NOVO CAMPO: Agência padrão
    @Column(name = "agency_number", nullable = false, length = 5)
    private String agencyNumber = "00001"; // Agência padrão
    
    @Column(name = "account_number", unique = true, nullable = false, length = 20)
    private String accountNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType type = AccountType.CORRENTE;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "daily_limit", precision = 15, scale = 2)
    private BigDecimal dailyLimit = new BigDecimal("1000.00");
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "transactional_password", length = 60)
    private String transactionalPassword;
    
    @Column(name = "password_set", nullable = false)
    private boolean passwordSet = false;
    
    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;
    
    @Column(name = "password_blocked_until")
    private LocalDateTime passwordBlockedUntil;
    
    public enum AccountType {
        CORRENTE, POUPANCA, SALARIO
    }
    
    public enum AccountStatus {
        ACTIVE, INACTIVE, BLOCKED
    }
    
    

    
    
    public Account(Long userId, String userName, String userCpf, String userEmail, 
                   String userPhone, String accountNumber, AccountType type) {
        this.userId = userId;
        this.userName = userName;
        this.userCpf = userCpf;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
        this.agencyNumber = "00001";
        this.accountNumber = accountNumber;
        this.type = type;
        this.balance = BigDecimal.ZERO;
        this.dailyLimit = calculateDailyLimit(type);
        this.status = AccountStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.passwordSet = false; // ✅ Inicializar novos campos
        this.failedAttempts = 0;
        this.passwordBlockedUntil = null;
    }



    public Account() {
    }

    private BigDecimal calculateDailyLimit(AccountType type) {
        return switch (type) {
            case CORRENTE -> new BigDecimal("1000.00");
            case POUPANCA -> new BigDecimal("500.00");
            case SALARIO -> new BigDecimal("2000.00");
            default -> new BigDecimal("1000.00"); // Default
        };
    }


    // ✅ MÉTODO PARA FORMATAR AGÊNCIA/CONTA COMPLETA
    public String getFullAccountNumber() {
        return this.agencyNumber + " / " + this.accountNumber;
    }
    
    // ✅ MÉTODO PARA OBTER CÓDIGO DO BANCO 
    public String getBankCode() {
        return "711";
    }
    
    // ✅ MÉTODO PARA FORMATO COMPLETO BANCO/AGÊNCIA/CONTA
    public String getBankDetails() {
        return getBankCode() + " / " + getFullAccountNumber();
    }
    
    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do depósito deve ser positivo");
        }
        this.balance = this.balance.add(amount);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do saque deve ser positivo");
        }
        if (amount.compareTo(balance) > 0) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }
        this.balance = this.balance.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean hasSufficientBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return this.balance.compareTo(amount) >= 0;
    }
    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserCpf() {
        return userCpf;
    }

    public void setUserCpf(String userCpf) {
        this.userCpf = userCpf;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getAgencyNumber() {
        return agencyNumber;
    }

    public void setAgencyNumber(String agencyNumber) {
        this.agencyNumber = agencyNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

     public void setTransactionalPassword(String password, PasswordEncoder passwordEncoder) {
        if (password == null || !password.matches("\\d{6}")) {
            throw new IllegalArgumentException("Senha deve conter exatamente 6 dígitos");
        }
        this.transactionalPassword = passwordEncoder.encode(password);
        this.passwordSet = true;
        this.failedAttempts = 0;
        this.passwordBlockedUntil = null;
    }
    
    // ✅ MÉTODO PARA VALIDAR SENHA TRANSACIONAL
    public boolean validateTransactionalPassword(String password, PasswordEncoder passwordEncoder) {
        if (passwordBlockedUntil != null && LocalDateTime.now().isBefore(passwordBlockedUntil)) {
            throw new RuntimeException("Senha bloqueada. Tente novamente em " + 
                ChronoUnit.MINUTES.between(LocalDateTime.now(), passwordBlockedUntil) + " minutos");
        }
        
        if (transactionalPassword == null || !passwordSet) {
            throw new RuntimeException("Senha transacional não configurada");
        }
        
        boolean isValid = passwordEncoder.matches(password, transactionalPassword);
        
        if (!isValid) {
            failedAttempts++;
            if (failedAttempts >= 3) {
                passwordBlockedUntil = LocalDateTime.now().plusMinutes(30); // Bloqueia por 30 minutos
                throw new RuntimeException("Senha bloqueada por 30 minutos devido a múltiplas tentativas falhas");
            }
            throw new RuntimeException("Senha incorreta. Tentativas restantes: " + (3 - failedAttempts));
        } else {
            failedAttempts = 0; // Reseta tentativas falhas
            passwordBlockedUntil = null;
        }
        
        return true;
    }
    
    // ✅ VERIFICAR SE SENHA ESTÁ CONFIGURADA
    public boolean isTransactionalPasswordSet() {
        return passwordSet;
    }



    public String getTransactionalPassword() {
        return transactionalPassword;
    }



    public void setTransactionalPassword(String transactionalPassword) {
        this.transactionalPassword = transactionalPassword;
    }



    public boolean isPasswordSet() {
        return passwordSet;
    }



    public void setPasswordSet(boolean passwordSet) {
        this.passwordSet = passwordSet;
    }



    public int getFailedAttempts() {
        return failedAttempts;
    }



    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }



    public LocalDateTime getPasswordBlockedUntil() {
        return passwordBlockedUntil;
    }



    public void setPasswordBlockedUntil(LocalDateTime passwordBlockedUntil) {
        this.passwordBlockedUntil = passwordBlockedUntil;
    }

    
    

}