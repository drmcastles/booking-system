package com.example.bookingservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    // Создаем ключ ОДИН РАЗ через Base64 декодирование
    private final Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key) // Используем Base64 ключ
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String username = claims.getSubject();
                String role = claims.get("role", String.class);
                if (role == null) role = "USER";

                // Очистка роли
                String cleanRole = role.replace("[", "").replace("]", "").replace("ROLE_", "").trim();

                if (username != null) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            username, null, Collections.singletonList(new SimpleGrantedAuthority(cleanRole))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    System.out.println(">>> BOOKING AUTH: " + username + " as " + cleanRole);
                }
            } catch (Exception e) {

                System.out.println(">>> BOOKING SECURITY ERROR: " + e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}