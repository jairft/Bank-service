package com.accountservice.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.accountservice.dto.ResetTransactionalPasswordRequest;
import com.accountservice.dto.TransactionalPasswordRequest;
import com.accountservice.dto.UpdateTransactionalPasswordRequest;
import com.accountservice.service.TransactionalPasswordService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactional-password")
public class TransactionalPasswordController {
    
    private final TransactionalPasswordService passwordService;
    
    public TransactionalPasswordController(TransactionalPasswordService passwordService) {
        this.passwordService = passwordService;
    }
    
    // ✅ DEFINIR SENHA PELA PRIMEIRA VEZ
    @PostMapping("/set")
    public ResponseEntity<Object> setTransactionalPassword(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody TransactionalPasswordRequest request) {

        // Lógica de negócio no service
        passwordService.setTransactionalPassword(userId, request);

        // Retorno JSON padronizado em caso de sucesso
        return ResponseEntity.ok(Map.of("message", "Senha transacional cadastrada com sucesso"));
    }

    // ✅ ALTERAR SENHA
    @PutMapping("/change")
    public ResponseEntity<Object> changeTransactionalPassword(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateTransactionalPasswordRequest request) {

        // Lógica de negócio no service
        passwordService.changeTransactionalPassword(userId, request);

        // Retorno JSON padronizado em caso de sucesso
        return ResponseEntity.ok(Map.of("message", "Senha transacional alterada com sucesso"));
    }



    // ✅ VERIFICAR STATUS
    @GetMapping("/status")
    public ResponseEntity<String> getPasswordStatus(@RequestHeader("X-User-Id") Long userId) {
        String status = passwordService.getPasswordStatus(userId);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/resend-token")
    public ResponseEntity<Map<String, String>> resendTransactionalToken(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-User-Id") Long userId) {

        passwordService.resendActivationTokenByUserId(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Um novo token foi enviado para o usuário logado.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetTransactionalPassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ResetTransactionalPasswordRequest request) {

        passwordService.resetTransactionalPasswordByUserId(userId, request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Senha transacional redefinida com sucesso!");
        return ResponseEntity.ok(response);
    }



}