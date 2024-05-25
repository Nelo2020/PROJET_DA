package com.DA.DA.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Component;

@Component
public class JwtKeyProvider {

    private final KeyPair keyPair;

    public JwtKeyProvider() {
        this.keyPair = generateRSAKeyPair();
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    private KeyPair generateRSAKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating RSA key pair", e);
        }
    }
}
