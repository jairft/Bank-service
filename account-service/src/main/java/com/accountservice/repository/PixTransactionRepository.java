package com.accountservice.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.accountservice.model.PixTransaction;

@Repository
public interface PixTransactionRepository extends JpaRepository<PixTransaction, Long> {
    
    Optional<PixTransaction> findByTransactionId(String transactionId);

    List<PixTransaction> findByFromAccountIdOrToAccountId(Long fromAccountId, Long toAccountId);
    
    List<PixTransaction> findByFromUserId(Long fromUserId);
    
    List<PixTransaction> findByToUserId(Long toUserId);
    
    List<PixTransaction> findByStatus(PixTransaction.TransactionStatus status);
    
    @Query("SELECT pt FROM PixTransaction pt WHERE pt.createdAt BETWEEN :startDate AND :endDate")
    List<PixTransaction> findTransactionsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
}
