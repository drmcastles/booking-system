package com.example.bookingservice.service;

import com.example.bookingservice.client.HotelClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final HotelClient hotelClient;

    public List<Long> getTopRecommendations(Long hotelId) {
        log.info(">>> [RECO] Запрос рекомендаций для отеля {}", hotelId);

        // Передаем LocalDateTime, чтобы не было ошибки несовместимости типов
        LocalDateTime today = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrow = LocalDate.now().plusDays(1).atStartOfDay();

        try {
            Object response = hotelClient.getAvailableRooms(hotelId, today, tomorrow);
            List<Long> roomIds = new ArrayList<>();

            if (response == null) return roomIds;

            // Безопасный разбор ответа
            if (response instanceof List) {
                List<?> list = (List<?>) response;
                for (Object item : list) {
                    if (item != null) roomIds.add(Long.valueOf(item.toString()));
                }
            } else if (response instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) response;
                Object values = map.get("value");
                if (values instanceof List) {
                    for (Object v : (List<?>) values) {
                        if (v != null) roomIds.add(Long.valueOf(v.toString()));
                    }
                }
            }

            log.info(">>> [RECO] Найдено комнат: {}", roomIds.size());
            return roomIds;

        } catch (Exception e) {
            log.error(">>> [RECO ERROR] Не удалось получить рекомендации: {}", e.getMessage());
            return List.of();
        }
    }
}