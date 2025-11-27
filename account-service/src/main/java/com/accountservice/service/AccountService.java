package com.accountservice.service;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.accountservice.model.Account;
import com.accountservice.repository.AccountRepository;

@Service
public class AccountService {
    
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    private final AccountRepository accountRepository;
    
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    
    @Transactional
    public Account createAccountForUser(Long userId, String userName, String userCpf, 
                                      String userEmail, String userPhone, Account.AccountType type) {
        
        System.out.println("Criando conta para usuário: " + userName + " (" + userEmail + ")");
        
        // Verifica se já existe conta para o usuário
        if (accountRepository.existsByUserId(userId)) {
            System.out.println("Usuário ID: " + userId + " já possui conta. Criando conta adicional.");
        }
        
        // Gera número da conta único
        String accountNumber = generateAccountNumber();
        
        // ✅ USA O CONSTRUTOR CORRETO (sem agencyNumber como parâmetro)
        Account account = new Account(userId, userName, userCpf, userEmail, userPhone, 
                                    accountNumber, type);
        
        Account savedAccount = accountRepository.save(account);
        
        System.out.println("✅ CONTA BANCÁRIA CRIADA");
        System.out.println("   🏦 Agência: " + savedAccount.getAgencyNumber()); // Já vem com "00001"
        System.out.println("   📊 Conta: " + savedAccount.getAccountNumber());
        System.out.println("   👤 Titular: " + savedAccount.getUserName());
        System.out.println("   💰 Tipo: " + savedAccount.getType());
        System.out.println("=========================================");
        
        return savedAccount;
    }

    @Transactional
    public Account updateAccountUser(Long userId, String newEmail, String newPhone) {
        Account account = getPrimaryAccount(userId); // Pega a conta principal do usuário

        boolean updated = false;

        if (newEmail != null && !newEmail.equals(account.getUserEmail())) {
            account.setUserEmail(newEmail);
            updated = true;
        }

        if (newPhone != null && !newPhone.equals(account.getUserPhone())) {
            account.setUserPhone(newPhone);
            updated = true;
        }

        if (!updated) {
            log.info("Nenhuma alteração detectada para a conta do usuário {}", userId);
            return account;
        }

        Account updatedAccount = accountRepository.save(account);
        log.info("Conta do usuário {} atualizada com sucesso", userId);
        log.info("   📧 Email: {}", updatedAccount.getUserEmail());
        log.info("   📱 Telefone: {}", updatedAccount.getUserPhone());

        return updatedAccount;
    }

    
    public Optional<Account> getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }
    
    public Optional<Account> getAccountById(Long accountId) {
        return accountRepository.findById(accountId);
    }
    
    @Transactional
    public Account deposit(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Conta não encontrada: " + accountNumber));
        
        account.deposit(amount);
        return accountRepository.save(account);
    }
    
    @Transactional
    public Account withdraw(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Conta não encontrada: " + accountNumber));
        
        account.withdraw(amount);
        return accountRepository.save(account);
    }
    
    public BigDecimal getBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Conta não encontrada: " + accountNumber));
        
        return account.getBalance();
    }
    
    private String generateAccountNumber() {
        Random random = new Random();
        String accountNumber;
        
        do {
            // Gera número no formato: 00001-1 (5 dígitos + 1 dígito verificador)
            int numero = 10000 + random.nextInt(90000);
            int digito = random.nextInt(10);
            accountNumber = String.format("%05d-%d", numero, digito);
        } while (accountRepository.existsByAccountNumber(accountNumber));
        
        return accountNumber;
    }


    public Account updateAccount(Account account) {
        return accountRepository.save(account);
    }

    public Account getPrimaryAccount(Long userId) {
        Optional<Account> accountOpt = accountRepository.findByUserId(userId)
            .stream()
            .findFirst(); // assume que a primeira conta é a principal

        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Conta principal do usuário " + userId + " não encontrada");
        }

        return accountOpt.get();
    }

    /**
     * Retorna todas as contas do usuário (opcional)
     */
    public List<Account> getUserAccounts(Long userId) {
        return accountRepository.findByUserId(userId);
    }
    
}