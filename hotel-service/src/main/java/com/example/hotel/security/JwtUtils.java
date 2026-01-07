package com.example.hotel.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;

@Component
public class JwtUtils {
    // Тот же ключ, что и в Booking Service!
    private final String SECRET_STRING = "SuperSecretKeyForBookingSystem1234567890123456";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}