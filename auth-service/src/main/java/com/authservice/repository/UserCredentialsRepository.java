package com.authservice.repository;

import com.authservice.model.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserCredentialsRepository extends JpaRepository<UserCredentials, Long> {
    Optional<UserCredentials> findByEmail(String email);
    Optional<UserCredentials> findByUserId(Long userId);
    boolean existsByEmail(String email);
    boolean existsByUserId(Long userId);

    Optional<UserCredentials> findByActivationToken(String activationToken);

    @Query("SELECT uc FROM UserCredentials uc WHERE uc.activationToken = :token AND uc.activationExpires > :now")
    Optional<UserCredentials> findByActivationTokenAndNotExpired(@Param("token") String token, @Param("now") LocalDateTime now);
}
