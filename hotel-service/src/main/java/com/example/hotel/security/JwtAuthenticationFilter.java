package com.example.hotel.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Извлекаем заголовок (поддержка разных регистров для Gateway)
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
            authHeader = request.getHeader("authorization");
        }

        // 2. Проверка наличия заголовка и формата Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (authHeader == null) {
                System.out.println("[HOTEL-SERVICE DEBUG] No Authorization header found.");
            } else {
                System.out.println("[HOTEL-SERVICE DEBUG] Header found but NOT starting with Bearer.");
            }
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        System.out.println("[HOTEL-SERVICE DEBUG] JWT Token extracted. Starting validation...");

        try {
            // 3. Валидация токена
            if (jwtUtils.validateToken(jwt)) {
                String username = jwtUtils.extractUsername(jwt);
                String role = jwtUtils.extractRole(jwt);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Форматируем роль для Spring Security
                    String finalRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    var authority = new SimpleGrantedAuthority(finalRole);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username, null, List.of(authority)
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    System.out.println("[HOTEL-SERVICE DEBUG] SUCCESS! Authenticated: " + username + " with role: " + finalRole);
                }
            } else {
                // Если JwtUtils вернул false
                System.out.println("[HOTEL-SERVICE DEBUG] FAILED: JwtUtils.validateToken returned false.");
            }
        } catch (Exception e) {
            // Если возникла критическая ошибка (неверная подпись, истек срок и т.д.)
            System.err.println("[HOTEL-SERVICE DEBUG] EXCEPTION during JWT processing: " + e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}