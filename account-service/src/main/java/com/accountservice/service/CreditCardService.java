package com.accountservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.accountservice.dto.CardSummaryResponse;
import com.accountservice.dto.CreateCardRequest;
import com.accountservice.dto.CreateCardResponse;
import com.accountservice.dto.UnblockCardRequest;
import com.accountservice.exception.CardNotFoundException;
import com.accountservice.model.Account;
import com.accountservice.model.CardBrand;
import com.accountservice.model.CardStatus;
import com.accountservice.model.CreditCard;
import com.accountservice.repository.AccountRepository;
import com.accountservice.repository.CreditCardRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CreditCardService {

    private static final Logger log = LoggerFactory.getLogger(CreditCardService.class);

    private final CreditCardRepository repo;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    private static final BigDecimal MIN_LIMIT = BigDecimal.valueOf(3000);
    private static final BigDecimal MAX_LIMIT = BigDecimal.valueOf(30000);

    public CreditCardService(
            CreditCardRepository repo,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.repo = repo;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------------------------------------------------------------
    // 🔹 CRIAR CARTÃO
    // ---------------------------------------------------------------
    @Transactional
    public CreateCardResponse createCard(CreateCardRequest req, Long userId) {
        BigDecimal requested = req.getRequestedLimit() == null ? MIN_LIMIT : req.getRequestedLimit();
        BigDecimal approved = determineApprovedLimit(requested);

        // Simula processamento de aprovação (1 a 2 minutos)
        try {
            long delay = 30_000 + random.nextInt(30_000); // entre 1 e 2 minutos
            log.info("⏳ Simulando análise de crédito por {} ms...", delay);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Gera dados do cartão
        String cardNumber = generateCardNumber(req.getBrand());
        String masked = maskCardNumber(cardNumber);
        String cvv = generateCVV(req.getBrand());
        String expiry = generateExpiry(3, 5);

        CreditCard card = new CreditCard();
        card.setUserId(userId);
        card.setBrand(req.getBrand());
        card.setCardNumber(cardNumber);
        card.setCardNumberMasked(masked);
        card.setCvv(cvv);
        card.setExpiry(expiry);
        card.setApprovedLimit(approved);
        card.setStatus(CardStatus.BLOCKED);
        card.setCreatedAt(LocalDateTime.now());

        CreditCard saved = repo.save(card);
        log.info("✅ Cartão criado (aguardando desbloqueio): {}", saved.getCardNumberMasked());

        // Resposta para o front
        CreateCardResponse resp = new CreateCardResponse();
        resp.setCardId(saved.getId());
        resp.setBrand(saved.getBrand().name());
        resp.setCardNumberMasked(saved.getCardNumberMasked());
        resp.setCardNumberFull(saved.getCardNumber());
        resp.setExpiry(saved.getExpiry());
        resp.setCvv(saved.getCvv());
        resp.setApprovedLimit(saved.getApprovedLimit());
        resp.setCreatedAt(saved.getCreatedAt());
        return resp;
    }

    // ---------------------------------------------------------------
    // 🔹 DESBLOQUEAR CARTÃO (CVV + senha transacional)
    // ---------------------------------------------------------------
    @Transactional
    public void unblockCard(Long userId, Long cardId, UnblockCardRequest req) {
        CreditCard card = repo.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Cartão não encontrado"));

        if (card.getStatus() != CardStatus.BLOCKED) {
            throw new CardNotFoundException("Cartão já está ativo");
        }

        // valida CVV (aqui não usa passwordEncoder, é valor simples)
        if (!card.getCvv().equals(req.getCvv())) {
            throw new CardNotFoundException("CVV inválido");
        }

        // valida senha transacional
        Account account = accountRepository.findFirstByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));

        if (account.getTransactionalPassword() == null ||
            !passwordEncoder.matches(req.getTransactionalPassword(), account.getTransactionalPassword())) {
            throw new CardNotFoundException("Senha transacional inválida");
        }

        // tudo OK -> ativa o cartão
        card.setStatus(CardStatus.ACTIVE);
        repo.save(card);

        log.info("💳 Cartão {} desbloqueado com sucesso para usuário {}", card.getCardNumberMasked(), userId);
    }

    // ---------------------------------------------------------------
    // 🔹 LISTAR CARTÕES DO USUÁRIO
    // ---------------------------------------------------------------
    public List<CardSummaryResponse> listUserCards(Long userId) {
        return repo.findByUserId(userId).stream().map(c -> {
            CardSummaryResponse s = new CardSummaryResponse();
            s.setCardId(c.getId());
            s.setBrand(c.getBrand().name());
            s.setCardNumberMasked(c.getCardNumberMasked());
            s.setCardNumberFull(c.getCardNumber()); // retorno completo
            s.setExpiry(c.getExpiry());
            s.setCvv(c.getCvv());
            s.setApprovedLimit(c.getApprovedLimit());
            s.setStatus(c.getStatus().name());
            s.setCreatedAt(c.getCreatedAt());
            return s;
        }).collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // 🔹 DETALHAR UM CARTÃO ESPECÍFICO
    // ---------------------------------------------------------------
    public CreateCardResponse getCardDetails(Long userId, Long cardId) {
        CreditCard card = repo.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Cartão não encontrado"));
        if (!card.getUserId().equals(userId)) {
            throw new CardNotFoundException("Cartão não pertence a este usuário");
        }

        CreateCardResponse r = new CreateCardResponse();
        r.setCardId(card.getId());
        r.setBrand(card.getBrand().name());
        r.setCardNumberMasked(card.getCardNumberMasked());
        r.setCardNumberFull(card.getCardNumber());
        r.setExpiry(card.getExpiry());
        r.setCvv(card.getCvv());
        r.setApprovedLimit(card.getApprovedLimit());
        r.setCreatedAt(card.getCreatedAt());
        return r;
    }

    // ---------------------------------------------------------------
    // 🔹 MÉTODOS AUXILIARES
    // ---------------------------------------------------------------

    private BigDecimal determineApprovedLimit(BigDecimal requested) {
        if (requested.compareTo(MIN_LIMIT) <= 0) return MIN_LIMIT;
        if (requested.compareTo(MAX_LIMIT) >= 0) return MAX_LIMIT;

        BigDecimal range = requested.subtract(MIN_LIMIT);
        long minCents = MIN_LIMIT.movePointRight(2).longValue();
        long rangeCents = range.movePointRight(2).longValue();
        long randomCents = minCents + (long) (random.nextDouble() * (rangeCents + 1));
        return BigDecimal.valueOf(randomCents / 100).setScale(0, RoundingMode.DOWN);

    }

    private String generateCardNumber(CardBrand brand) {
        String prefix;
        switch (brand) {
            case VISA -> prefix = "4";
            case MASTERCARD -> prefix = "5";
            case ELO -> prefix = "636";
            case AMEX -> prefix = "34";
            default -> prefix = "4";
        }

        int totalLen = 16;
        StringBuilder sb = new StringBuilder(prefix);
        while (sb.length() < totalLen - 1) {
            sb.append(random.nextInt(10));
        }
        int check = calculateLuhnCheckDigit(sb.toString());
        sb.append(check);
        return sb.toString();
    }

    private int calculateLuhnCheckDigit(String numberWithoutCheck) {
        int sum = 0;
        boolean alternate = true;
        for (int i = numberWithoutCheck.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(numberWithoutCheck.charAt(i));
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        int mod = sum % 10;
        return (10 - mod) % 10;
    }

    private String maskCardNumber(String full) {
        if (full == null || full.length() < 8) return full;
        return full.substring(0, 4) + " **** **** " + full.substring(full.length() - 4);
    }

    private String generateCVV(CardBrand brand) {
        int digits = (brand == CardBrand.AMEX) ? 4 : 3;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digits; i++) sb.append(random.nextInt(10));
        return sb.toString();
    }

    private String generateExpiry(int minYears, int maxYears) {
        int years = minYears + random.nextInt(maxYears - minYears + 1);
        LocalDateTime d = LocalDateTime.now().plusYears(years);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yy");
        return d.format(fmt);
    }
}
