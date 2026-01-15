package com.example.bookingservice.controller;

import com.example.bookingservice.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/{hotelId}")
    public List<Long> getRecommendations(@PathVariable Long hotelId) {
        // Теперь сервис возвращает List<Long>, и контроллер тоже
        return recommendationService.getTopRecommendations(hotelId);
    }
}