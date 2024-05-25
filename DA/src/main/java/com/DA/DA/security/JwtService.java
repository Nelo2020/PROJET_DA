package com.DA.DA.security;

import com.DA.DA.entite.Da;
import com.DA.DA.service.EmployeeService;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.security.*;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service

public class JwtService {

    private final JwtKeyProvider jwtKeyProvider;
    private final EmployeeService employeeService;
    private final JwtParserBuilder jwtParserBuilder;

    @Autowired
    public JwtService(JwtKeyProvider jwtKeyProvider, EmployeeService employeeService) {
        this.jwtKeyProvider = jwtKeyProvider;
        this.employeeService = employeeService;
        this.jwtParserBuilder = Jwts.parser().setSigningKey(jwtKeyProvider.getKeyPair().getPublic());
    }


    public Jws<Claims> validateToken(String token) {
        return jwtParserBuilder
                .setSigningKey(jwtKeyProvider.getKeyPair().getPublic())
                .build()
                .parseClaimsJws(token);
    }

    public Claims decryptClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(jwtKeyProvider.getKeyPair().getPublic())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new JwtException("Failed to parse JWT token", e);
        }
    }
    public String lireEmployeEmail(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public boolean isTokenExpired(String token) {
        Date expirationDate = getClaim(token, Claims::getExpiration);
        return expirationDate != null && expirationDate.before(new Date());
    }

    public Map<String, String> generate(String email) {
        Da da = (Da) employeeService.loadUserByUsername(email);
        return generateJwt(da);
    }

    public Map<String, String> generateJwt(Da da) {
        final long expirationTime = System.currentTimeMillis() + 40 * 60 * 1000; // 40 minutes

        String nomEncrypted = encryptSensitiveData(da.getNom());

        String emailEncrypted = encryptSensitiveData(da.getEmail());
        String roleEncrypted = encryptSensitiveData(String.valueOf(da.getRole().getLibelle()));
        String posteEncrypted = encryptSensitiveData(da.getPoste());
        String prenomsEncrypted = encryptSensitiveData(da.getPrenoms());
        String usernameEncrypted = encryptSensitiveData(da.getUsername());

        final Map<String, Object> claims = Map.of(
                "nom", nomEncrypted,
                "email", emailEncrypted,
                "role", roleEncrypted,
                "poste", posteEncrypted,
                "id", da.getId(),
                "prenom", prenomsEncrypted,
                "username", usernameEncrypted,
                Claims.EXPIRATION, new Date(expirationTime),
                Claims.SUBJECT, da.getEmail()
        );

        final String bearer = Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(new Date(expirationTime))
                .setSubject(da.getEmail())
                .setClaims(claims)
                .signWith(jwtKeyProvider.getKeyPair().getPrivate(), SignatureAlgorithm.RS256)
                .compact();

        return Map.of("bearer", bearer);
    }


    private <T> T getClaim(String token, Function<Claims, T> function) {
        Claims claims = getAllClaims(token);
        return function.apply(claims);
    }


    Claims getAllClaims(String token) {
        return jwtParserBuilder
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String encryptSensitiveData(String data) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, jwtKeyProvider.getKeyPair().getPublic());
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting sensitive data", e);
        }
    }

    String decryptSensitiveData(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, jwtKeyProvider.getKeyPair().getPrivate());
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting sensitive data", e);
        }
    }


    public Long extractIdFromJwt(String token) {
        Claims claims = getAllClaims(token);
        return claims.get("id", Long.class);
    }

    public String extractEmailFromJwt(String token) {
        Claims claims = getAllClaims(token);
        return claims.get("email", String.class);
    }
}
