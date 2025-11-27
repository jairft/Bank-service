package com.accountservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.accountservice.model.CreditCard;

public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {
    
    List<CreditCard> findByUserId(Long userId);
    long countByUserId(Long userId);
}

