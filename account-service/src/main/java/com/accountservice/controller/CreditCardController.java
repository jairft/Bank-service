package com.accountservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.accountservice.dto.CardSummaryResponse;
import com.accountservice.dto.CreateCardRequest;
import com.accountservice.dto.CreateCardResponse;
import com.accountservice.dto.UnblockCardRequest;
import com.accountservice.service.CreditCardService;

@RestController
@RequestMapping("/api/cards")
public class CreditCardController {

    private final CreditCardService service;

    public CreditCardController(CreditCardService service) {
        this.service = service;
    }

    // 🔹 Solicitar novo cartão (demora simulada 1–2 minutos)
    @PostMapping
    public ResponseEntity<CreateCardResponse> createCard(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CreateCardRequest request) {
        return ResponseEntity.ok(service.createCard(request, userId));
    }

    // 🔹 Listar cartões do usuário (com CVV e número visíveis)
    @GetMapping
    public ResponseEntity<List<CardSummaryResponse>> listUserCards(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(service.listUserCards(userId));
    }

    // 🔹 Detalhes de um cartão específico
    @GetMapping("/{cardId}/details")
    public ResponseEntity<CreateCardResponse> getCardDetails(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long cardId) {
        return ResponseEntity.ok(service.getCardDetails(userId, cardId));
    }

    // 🔹 Desbloquear cartão (usa CVV e senha transacional)
    @PostMapping("/{cardId}/unblock")
    public ResponseEntity<String> unblockCard(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long cardId,
            @RequestBody UnblockCardRequest request) {
        service.unblockCard(userId, cardId, request);
        return ResponseEntity.ok("Cartão desbloqueado com sucesso!");
    }
}
