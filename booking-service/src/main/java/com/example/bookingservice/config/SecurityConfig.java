package com.example.bookingservice.config;

import com.example.bookingservice.security.JwtAuthenticationFilter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Разрешаем доступ к Swagger и документации (включая путь через Gateway)
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/api/bookings/v3/api-docs",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/h2-console/**"
                        ).permitAll()

                        // Строгое соответствие "ADMIN"
                        .requestMatchers("/api/bookings/all").hasAuthority("ADMIN")
                        .requestMatchers("/api/bookings/admin/**").hasAuthority("ADMIN")

                        // Доступ для USER и ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/bookings/create").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/bookings/my/**").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers("/api/recommendations/**").hasAnyAuthority("USER", "ADMIN")

                        // Обновление и удаление
                        .requestMatchers(HttpMethod.PUT, "/api/bookings/**").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/bookings/**").hasAnyAuthority("USER", "ADMIN")

                        .anyRequest().authenticated()
                )
                // Добавляем фильтр JWT
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // Нужно для работы консоли H2
                .headers(h -> h.frameOptions(f -> f.disable()))
                .build();
    }

    /**
     * Конфигурация OpenAPI для Swagger UI.
     * Добавляет кнопку "Authorize" (Bearer Token) в интерфейс Swagger.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}