package com.example.booking.controller;

import com.example.booking.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public String login(@RequestParam String username) {
        return jwtUtils.generateToken(username);
    }
}