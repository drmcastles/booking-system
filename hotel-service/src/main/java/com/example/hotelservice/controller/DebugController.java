package com.example.hotelservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;

@RestController
public class DebugController {

    @Value("${jwt.secret:NOT_FOUND}")
    private String secret;

    @GetMapping("/api/debug/key")
    public String getKeyHash() {
        try {
            // Берем тот секрет, который реально видит Spring
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            return "Key: [" + secret.substring(0, 5) + "...], Hash: " + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}