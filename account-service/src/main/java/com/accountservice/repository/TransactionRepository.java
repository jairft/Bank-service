package com.accountservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.accountservice.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Optional<Transaction> findByTransactionId(String transactionId);
    
    List<Transaction> findByAccountId(Long accountId);
    
    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId ORDER BY t.createdAt DESC")
    List<Transaction> findRecentTransactionsByAccountId(@Param("accountId") Long accountId);
}