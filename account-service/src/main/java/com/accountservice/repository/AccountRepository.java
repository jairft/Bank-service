package com.accountservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.accountservice.model.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findByUserEmail(String email);
    
    Optional<Account> findFirstByUserId(Long userId);

    Optional<Account> findByActivationToken(String activationToken);
    
    List<Account> findByUserId(Long userId);
    
    Optional<Account> findByUserIdAndType(Long userId, Account.AccountType type);
    
    boolean existsByUserId(Long userId);
    
    boolean existsByAccountNumber(String accountNumber);
    
    @Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.status = 'ACTIVE'")
    List<Account> findActiveAccountsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COALESCE(COUNT(a), 0) FROM Account a WHERE a.userId = :userId")
    long countByUserId(@Param("userId") Long userId);
}