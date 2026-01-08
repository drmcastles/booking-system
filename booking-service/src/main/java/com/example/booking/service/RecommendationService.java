package com.example.booking.service;

import com.example.booking.client.HotelClient;
import com.example.booking.dto.RoomDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final HotelClient hotelClient;

    /**
     * Алгоритм рекомендаций (по ТЗ):
     * Возвращает список номеров, отсортированный по возрастанию times_booked
     * (при равенстве — по идентификатору).
     */
    public List<RoomDto> getRecommendations() {
        log.info(">>> Запуск алгоритма рекомендаций (приоритет свободных номеров)...");
        try {
            List<RoomDto> rooms = hotelClient.getRecommendedRooms();

            List<RoomDto> sortedRecommendations = rooms.stream()
                    // Сначала по количеству бронирований (от 0 и выше)
                    // Затем по ID (если бронирований поровну)
                    .sorted(Comparator.comparingInt(RoomDto::getTimesBooked)
                            .thenComparingLong(RoomDto::getId))
                    .collect(Collectors.toList());

            log.info(">>> Отсортировано {} рекомендаций для равномерной нагрузки", sortedRecommendations.size());
            return sortedRecommendations;
        } catch (Exception e) {
            log.error(">>> Ошибка при получении рекомендаций: {}", e.getMessage());
            return List.of();
        }
    }
}