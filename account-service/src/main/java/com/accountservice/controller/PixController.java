package com.accountservice.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.accountservice.dto.PixKeyRequest;
import com.accountservice.dto.PixKeyRequestInfo;
import com.accountservice.dto.PixKeyResponseInfo;
import com.accountservice.dto.PixTransferRequest;
import com.accountservice.dto.PixTransferResponse;
import com.accountservice.model.PixKey;
import com.accountservice.service.PixService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pix")
public class PixController {
    
    private static final Logger log = LoggerFactory.getLogger(PixController.class);
    private final PixService pixService;
    
    public PixController(PixService pixService) {
        this.pixService = pixService;
    }

    @PostMapping("/keys/register")
    public ResponseEntity<PixKey> registerPixKey(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody PixKeyRequest request) {

        PixKey pixKey = pixService.registerPixKey(userId, request.getKeyType());
        return ResponseEntity.ok(pixKey);
    }
    
    // LISTAR CHAVES PIX DO USUÁRIO
    @GetMapping("/keys")
    public ResponseEntity<List<PixKey>> getUserPixKeys(@RequestHeader("X-User-Id") Long userId) {
        log.info("Listando chaves PIX do usuário: {}", userId);
        List<PixKey> pixKeys = pixService.getUserPixKeys(userId);
        return ResponseEntity.ok(pixKeys);
    }
    
    // EXCLUIR CHAVE PIX
    @DeleteMapping("/keys/{keyId}")
    public ResponseEntity<String> deletePixKey(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long keyId) {

        log.info("Excluindo chave PIX {} do usuário: {}", keyId, userId);
        pixService.deletePixKey(userId, keyId);
        return ResponseEntity.ok("Chave PIX excluída com sucesso");
}

    
    // REALIZAR TRANSFERÊNCIA PIX
    @PostMapping("/transfer")
    public ResponseEntity<PixTransferResponse> transferPix(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String keyValue,             // valor da chave PIX (ex: maria@email.com)
            @Valid @RequestBody PixTransferRequest request) {

        System.out.println("💸 Transferência PIX do usuário: " + userId + " para chave " + keyValue);

        PixTransferResponse response = pixService.transferPix(
                userId,
                request,
                keyValue,
                request.getPassword() // senha vem do body
        );

        return ResponseEntity.ok(response);
    }

    
    // CONSULTAR CHAVE PIX
    @GetMapping("/keys/check/{keyType}/{keyValue}")
    public ResponseEntity<String> checkPixKey(
            @PathVariable String keyType,
            @PathVariable String keyValue) {
        
        log.info("Consultando chave PIX: {} {}", keyType, keyValue);
        // Esta funcionalidade será implementada no service
        return ResponseEntity.ok("Chave disponível para consulta");
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        log.error("Erro na operação PIX: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @PostMapping("/search")
    public PixKeyResponseInfo searchPixKey(@RequestBody PixKeyRequestInfo request) {
        return pixService.findPixKey(request);
    }
}