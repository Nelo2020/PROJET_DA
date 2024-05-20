package com.DA.DA.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.Cipher;

import com.DA.DA.entite.MembreAssurance;
import com.DA.DA.service.MembreAssuranceService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JwtServiceMembre {

    private final JwtKeyProvider jwtKeyProvider;
    @Autowired
    private MembreAssuranceService membreAssuranceService;

    @Autowired
    public JwtServiceMembre(JwtKeyProvider jwtKeyProvider) {
        this.jwtKeyProvider = jwtKeyProvider;
    }

    public Map<String, String> generate(String email) {
        MembreAssurance membreAssurance = (MembreAssurance) membreAssuranceService.loadUserByUsername(email);
        return generateJwt(membreAssurance);
    }

    public Map<String, String> generateJwt(MembreAssurance membreAssurance) {
        final long currentTime = System.currentTimeMillis();
        final long expirationTime = currentTime + 15 * 60 * 1000; // 15 minutes

        // Chiffrement des revendications sensibles
        String nomEncrypted = encryptSensitiveData(membreAssurance.getSociete().getNom());
        String emailEncrypted = encryptSensitiveData(membreAssurance.getEmail());
        //String posteEncrypted = encryptSensitiveData(membreAssurance.getPoste());
        String idDAEncrypted = encryptSensitiveData(String.valueOf(membreAssurance.getDa().getId()));
        String emailDAEncrypted = encryptSensitiveData(membreAssurance.getDa().getEmail());
        String idMembreEncrypted = encryptSensitiveData(String.valueOf(membreAssurance.getId_Membre()));
        String idSocieteEncrypted = encryptSensitiveData(String.valueOf(membreAssurance.getSociete().getId_societe()));
        String typeAssuranceEncrypted = encryptSensitiveData(String.valueOf(membreAssurance.getSociete().getTypeAssurance().getLibelles()));

        final Map<String, Object> claims = Map.of(
                "nom", nomEncrypted,
                "email", emailEncrypted,
                "poste", membreAssurance.getPoste(),
                "id_DA", idDAEncrypted,
                "email_DA", emailDAEncrypted,
                "id_membre", idMembreEncrypted,
                "id_societe", idSocieteEncrypted,
                "type_assurance", typeAssuranceEncrypted,

                Claims.EXPIRATION, expirationTime,
                Claims.SUBJECT, membreAssurance.getEmail()
        );

        final String bearer = Jwts.builder()
                .setIssuedAt(new Date(currentTime))
                .setExpiration(new Date(expirationTime))
                .setSubject(membreAssurance.getEmail())
                .setClaims(claims)
                .signWith(jwtKeyProvider.getKeyPair().getPrivate(), SignatureAlgorithm.RS256)
                .compact();

        return Map.of("bearer", bearer);
    }

    public String lireEmployeEmail(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public boolean isTokenExpired(String token) {
        Date expirationDate = getClaim(token, Claims::getExpiration);
        return expirationDate.before(new Date());
    }

    private <T> T getClaim(String token, Function<Claims, T> function) {
        Claims claims = getAllClaims(token);
        return function.apply(claims);
    }

    Claims getAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtKeyProvider.getKeyPair().getPublic())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long lireIdDaDuToken(String token) {
        Claims claims = getAllClaims(token);
        return Long.parseLong(decryptSensitiveData(claims.get("id_DA", String.class)));
    }

    public Long lireIdSocieteDuToken(String token) {
        Claims claims = getAllClaims(token);
        return Long.parseLong(decryptSensitiveData(claims.get("id_societe", String.class)));
    }

    public Long lireIdMembreDuToken(String token) {
        Claims claims = getAllClaims(token);
        return Long.parseLong(decryptSensitiveData(claims.get("id_membre", String.class)));
    }

    public String lireNomCompagnieDuToken(String token) {
        Claims claims = getAllClaims(token);
        return decryptSensitiveData(claims.get("nom", String.class));
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
}
