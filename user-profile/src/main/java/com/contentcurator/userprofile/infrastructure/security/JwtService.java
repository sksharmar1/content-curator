package com.contentcurator.userprofile.infrastructure.security;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
@Component
public class JwtService {
    @Value("${jwt.secret}") private String secret;
    @Value("${jwt.expiration-ms:86400000}") private long expirationMs;
    private SecretKey getKey() { return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); }
    public String generateToken(String userId, String email) {
        return Jwts.builder()
            .subject(userId)
            .claim("email", email)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(getKey())
            .compact();
    }
    public String extractUserId(String token) { return parseClaims(token).getSubject(); }
    public boolean isValid(String token) {
        try { parseClaims(token); return true; } catch (Exception e) { return false; }
    }
    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
    }
}
