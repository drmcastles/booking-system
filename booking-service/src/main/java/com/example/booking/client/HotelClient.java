package com.example.booking.client;

import com.example.booking.dto.RoomDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "hotel-service", url = "http://localhost:8081")
public interface HotelClient {

    // Шаг SAGA: подтверждение
    @PostMapping("/api/rooms/{id}/confirm-availability")
    boolean confirmAvailability(@PathVariable("id") Long id);

    // Шаг SAGA: отмена
    @PostMapping("/api/rooms/{id}/release")
    void releaseRoom(@PathVariable("id") Long id);

    // Для алгоритма рекомендаций
    @GetMapping("/api/rooms/recommend")
    List<RoomDto> getRecommendedRooms();
}