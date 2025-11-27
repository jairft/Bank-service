package com.accountservice.service;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

@Service
public class EncryptionService {

    private static final String SECRET = "1234567890123456"; // exemplo simples (16 bytes)
    private static final String ALGO = "AES";

    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance(ALGO);
            SecretKeySpec key = new SecretKeySpec(SECRET.getBytes(), ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar", e);
        }
    }

    public String decrypt(String encrypted) {
        try {
            Cipher cipher = Cipher.getInstance(ALGO);
            SecretKeySpec key = new SecretKeySpec(SECRET.getBytes(), ALGO);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar", e);
        }
    }
}
