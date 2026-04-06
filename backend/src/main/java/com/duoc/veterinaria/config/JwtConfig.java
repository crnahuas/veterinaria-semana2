package com.duoc.veterinaria.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtConfig {

    private final String secretKey;
    private final long expirationTime;

    public JwtConfig(
            @Value("${security.jwt.secret}") String secretKey,
            @Value("${security.jwt.expiration-ms:" + Constants.TOKEN_EXPIRATION_TIME + "}") long expirationTime) {
        this.secretKey = secretKey;
        this.expirationTime = expirationTime;
    }

    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("authorities", List.of("ROLE_" + role));

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuer(Constants.ISSUER_INFO)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(Constants.getSigningKey(secretKey))
                .compact();
    }

    public String generateBearerToken(String username, String role) {
        return Constants.TOKEN_BEARER_PREFIX + generateToken(username, role);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(Constants.getSigningKey(secretKey))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
