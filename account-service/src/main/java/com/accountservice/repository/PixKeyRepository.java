package com.accountservice.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.accountservice.model.PixKey;

@Repository
public interface PixKeyRepository extends JpaRepository<PixKey, Long> {
    
    List<PixKey> findByUserId(Long userId);

    Optional<PixKey> findByKeyValue(String keyValue);
    
    Optional<PixKey> findByKeyValueAndKeyType(String keyValue, PixKey.PixKeyType keyType);
    
    boolean existsByKeyValueAndKeyType(String keyValue, PixKey.PixKeyType keyType);
    
    boolean existsByUserIdAndKeyValueAndKeyType(Long userId, String keyValue, PixKey.PixKeyType keyType);
    
    @Query("SELECT pk FROM PixKey pk WHERE pk.userId = :userId AND pk.status = 'ACTIVE'")
    List<PixKey> findActiveKeysByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(pk) FROM PixKey pk WHERE pk.userId = :userId AND pk.status = 'ACTIVE'")
    long countActiveKeysByUserId(@Param("userId") Long userId);
}
