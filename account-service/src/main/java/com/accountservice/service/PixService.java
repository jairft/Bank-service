package com.accountservice.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.accountservice.dto.PixKeyRequestInfo;
import com.accountservice.dto.PixKeyResponseInfo;
import com.accountservice.dto.PixTransferRequest;
import com.accountservice.dto.PixTransferResponse;
import com.accountservice.exception.InsufficientBalanceException;
import com.accountservice.exception.InvalidPixKeyException;
import com.accountservice.exception.InvalidTransactionalPasswordException;
import com.accountservice.model.Account;
import com.accountservice.model.PixKey;
import com.accountservice.model.PixKey.PixKeyType;
import com.accountservice.model.PixTransaction;
import com.accountservice.repository.AccountRepository;
import com.accountservice.repository.PixKeyRepository;
import com.accountservice.repository.PixTransactionRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PixService {
    
    private static final Logger log = LoggerFactory.getLogger(PixService.class);

    @Value("${bank.info}")
    private String bankInfo;

    private final TransactionalPasswordService passwordService;
    private final AccountRepository accountRepository;
    private final PixKeyRepository pixKeyRepository;
    private final PixTransactionRepository pixTransactionRepository;
    private final AccountService accountService;
    
    
    public PixService(PixKeyRepository pixKeyRepository, 
                     PixTransactionRepository pixTransactionRepository,
                     AccountService accountService, TransactionalPasswordService passwordService, AccountRepository accountRepository) {
        this.pixKeyRepository = pixKeyRepository;
        this.pixTransactionRepository = pixTransactionRepository;
        this.accountService = accountService;
        this.passwordService = passwordService;
        this.accountRepository = accountRepository;
    }
    
    // CADASTRAR CHAVE PIX
    @Transactional
    public PixKey createPixKey(Long userId, String ownerName, PixKey.PixKeyType keyType, String keyValue) {
        log.info("Criando chave PIX para usuário {}: {} {}", userId, keyType, keyValue);
        
        // Validações
        validatePixKey(keyType, keyValue);
        
        // Verifica se chave já existe globalmente
        if (pixKeyRepository.existsByKeyValueAndKeyType(keyValue, keyType)) {
            throw new RuntimeException("Chave PIX já está cadastrada: " + keyValue);
        }
        
        // Verifica limite de chaves por usuário (máximo 5)
        long activeKeysCount = pixKeyRepository.countActiveKeysByUserId(userId);
        if (activeKeysCount >= 5) {
            throw new RuntimeException("Limite máximo de 5 chaves PIX atingido");
        }
        
        PixKey pixKey = new PixKey();
        pixKey.setUserId(userId);
        pixKey.setKeyType(keyType);
        pixKey.setKeyValue(keyValue);
        pixKey.setOwnerName(ownerName);
        pixKey.setStatus(PixKey.PixKeyStatus.ACTIVE);
        
        PixKey savedKey = pixKeyRepository.save(pixKey);
        log.info("Chave PIX criada com sucesso: ID {}", savedKey.getId());
        
        return savedKey;
    }

    @Transactional
    public PixKey registerPixKey(Long userId, PixKey.PixKeyType keyType) {
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

        String keyValue;
        switch (keyType) {
            case CPF -> keyValue = account.getUserCpf();
            case EMAIL -> keyValue = account.getUserEmail();
            case TELEFONE -> keyValue = account.getUserPhone();
            case RANDOM -> keyValue = java.util.UUID.randomUUID().toString();
            default -> throw new IllegalArgumentException("Tipo de chave inválido: " + keyType);
        }
        // Verifica se chave já existe globalmente
        if (pixKeyRepository.existsByKeyValueAndKeyType(keyValue, keyType)) {
            throw new RuntimeException("Chave PIX já está cadastrada: " + keyValue);
        }

        PixKey pixKey = new PixKey();
        pixKey.setUserId(userId);
        pixKey.setKeyType(keyType);
        pixKey.setKeyValue(keyValue);
        pixKey.setOwnerName(account.getUserName());

        return pixKeyRepository.save(pixKey);
    }
    
    // LISTAR CHAVES PIX DO USUÁRIO
    public List<PixKey> getUserPixKeys(Long userId) {
        return pixKeyRepository.findByUserId(userId);
    }
    
    // EXCLUIR CHAVE PIX
    @Transactional
    public void deletePixKey(Long userId, Long keyId) {

        PixKey pixKey = pixKeyRepository.findById(keyId)
            .orElseThrow(() -> new RuntimeException("Chave PIX não encontrada"));
        
        if (!pixKey.getUserId().equals(userId)) {
            throw new RuntimeException("Chave PIX não pertence ao usuário");
        }

        pixKeyRepository.delete(pixKey);
        log.info("Chave PIX excluída com sucesso: {}", keyId);
    }


    @Transactional
     public PixKeyResponseInfo findPixKey(PixKeyRequestInfo request) {
        PixKeyType type = request.getKeyType();
        String value = request.getKeyValue();

        PixKey pixKey = pixKeyRepository
                .findByKeyValueAndKeyType(value, type)
                .orElseThrow(() -> new EntityNotFoundException("Chave Pix não encontrada"));

        return new PixKeyResponseInfo(
                pixKey.getOwnerName(),
                pixKey.getKeyType(),
                pixKey.getKeyValue(),
                bankInfo
        );
    }

    @Transactional
    public PixTransferResponse transferPix(
            Long fromUserId,
            PixTransferRequest request,
            String keyValue,
            String transactionalPassword) {

        System.out.println("🔐 Validando senha transacional para transferência PIX...");

        // 1️⃣ Valida senha transacional
        if (!passwordService.authorizeTransaction(fromUserId, transactionalPassword)) {
            throw new InvalidTransactionalPasswordException("Senha transacional inválida");
        }

        System.out.println("✅ Senha validada. Processando transferência PIX...");

        // 2️⃣ Valida valor
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InsufficientBalanceException("Valor da transferência deve ser maior que zero");
        }

        // 3️⃣ Conta de origem
        Account fromAccount = getPrimaryAccount(fromUserId);

        // 4️⃣ Verifica saldo suficiente
        if (!fromAccount.hasSufficientBalance(request.getAmount())) {
            throw new InsufficientBalanceException("Saldo insuficiente para a transferência");
        }

        // 5️⃣ Busca chave PIX de destino (somente pelo valor da chave)
        PixKey destinationKey = pixKeyRepository.findByKeyValue(keyValue)
                .orElseThrow(() -> new InvalidPixKeyException("Chave PIX não encontrada: " + keyValue));

        if (destinationKey.getStatus() != PixKey.PixKeyStatus.ACTIVE) {
            throw new InvalidPixKeyException("Chave PIX está inativa");
        }

        // 6️⃣ Conta de destino
        Account toAccount = getPrimaryAccount(destinationKey.getUserId());

        // 7️⃣ Cria transação PIX
        PixTransaction transaction = createPixTransaction(
                fromUserId,
                fromAccount.getId(),
                destinationKey.getUserId(),
                toAccount.getId(),
                request,
                destinationKey
        );

        try {
            // 8️⃣ Executa transferência
            executeTransfer(fromAccount, toAccount, request.getAmount());

            // 9️⃣ Atualiza status
            transaction.setStatus(PixTransaction.TransactionStatus.COMPLETED);
            transaction.setProcessedAt(LocalDateTime.now());
            pixTransactionRepository.save(transaction);
     
            System.out.println("✅ Transferência PIX concluída: " + transaction.getTransactionId());

            // 🔟 Monta resposta
            String fromTo = "DE " + fromAccount.getUserName() + " PARA " + toAccount.getUserName();

            PixTransferResponse response = new PixTransferResponse();
            response.setTransactionId(transaction.getTransactionId());
            response.setStatus(transaction.getStatus().name());
            response.setAmount(request.getAmount());
            response.setFromAccount(fromTo);
            response.setTimestamp(transaction.getProcessedAt());
            response.setMessage("Transferência realizada com sucesso");

            return response;

        } catch (Exception e) {
            transaction.setStatus(PixTransaction.TransactionStatus.FAILED);
            pixTransactionRepository.save(transaction);
            System.err.println("❌ Erro na transferência PIX: " + e.getMessage());
            throw new RuntimeException("Falha na transferência: " + e.getMessage());
        }
    }


    
    // MÉTODOS AUXILIARES
    private void validatePixKey(PixKey.PixKeyType keyType, String keyValue) {
        switch (keyType) {
            case CPF:
                if (!keyValue.matches("\\d{11}")) {
                    throw new RuntimeException("CPF deve conter 11 dígitos");
                }
                break;
            case EMAIL:
                if (!keyValue.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    throw new RuntimeException("Email inválido");
                }
                break;
            case TELEFONE:
                if (!keyValue.matches("^\\d{10,11}$")) {
                    throw new RuntimeException("Telefone deve conter 10 ou 11 dígitos");
                }
                break;
            case RANDOM:
                if (keyValue.length() != 32) {
                    throw new RuntimeException("Chave aleatória deve ter 32 caracteres");
                }
                break;
        }
    }
    
    private Account getPrimaryAccount(Long userId) {
        List<Account> accounts = accountService.getUserAccounts(userId);
        return accounts.stream()
            .filter(acc -> acc.getStatus() == Account.AccountStatus.ACTIVE)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Usuário não possui conta ativa"));
    }
    
    private PixTransaction createPixTransaction(Long fromUserId, Long fromAccountId, 
                                            Long toUserId, Long toAccountId,
                                            PixTransferRequest request,
                                            PixKey destinationKey) { // ✅ adicionado

        PixTransaction transaction = new PixTransaction();
        transaction.setTransactionId(generateTransactionId());
        transaction.setFromUserId(fromUserId);
        transaction.setFromAccountId(fromAccountId);
        transaction.setToUserId(toUserId);
        transaction.setToAccountId(toAccountId);
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setStatus(PixTransaction.TransactionStatus.PROCESSING);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setKeyType(destinationKey.getKeyType());
        transaction.setPixKey(destinationKey.getKeyValue());

        return pixTransactionRepository.save(transaction);
    }

    
    private void executeTransfer(Account fromAccount, Account toAccount, BigDecimal amount) {
        // Debita da conta origem
        fromAccount.withdraw(amount);
        accountService.updateAccount(fromAccount);
        
        // Credita na conta destino
        toAccount.deposit(amount);
        accountService.updateAccount(toAccount);
    }
    
    private String generateTransactionId() {
        return "PIX" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    // Atualize o AccountService para incluir este método:
    // public Account updateAccount(Account account) {
    //     return accountRepository.save(account);
    // }
}