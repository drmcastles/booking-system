package com.example.bookingservice.client;

import com.example.bookingservice.config.FeignConfig;
import com.example.bookingservice.dto.ReservationRequest;
import com.example.bookingservice.dto.RoomDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Клиент для связи с HOTEL-SERVICE через Feign.
 */
@FeignClient(name = "hotel-service", configuration = FeignConfig.class)
public interface HotelClient {

    /**
     * Поиск свободных комнат.
     * Используем LocalDateTime, так как Booking Service передает время (T14:00).
     */
    @GetMapping("/api/hotels/rooms/hotel/{hotelId}/available")
    Object getAvailableRooms(
                              @PathVariable("hotelId") Long hotelId,
                              @RequestParam("checkIn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkIn,
                              @RequestParam("checkOut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOut
    );

    /**
     * Резервация комнаты (Шаг SAGA).
     */
    @PostMapping("/api/hotels/rooms/reserve")
    void reserveRoom(@RequestBody ReservationRequest request);
}