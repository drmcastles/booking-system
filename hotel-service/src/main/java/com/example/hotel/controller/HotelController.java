package com.example.hotel.controller;

import com.example.hotel.entity.Room;
import com.example.hotel.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HotelController {
    private final HotelService hotelService;

    // USER: рекомендованные номера
    @GetMapping("/rooms/recommend")
    public List<Room> getRecommended() {
        return hotelService.getRecommendedRooms();
    }

    // INTERNAL: подтверждение доступности (используется Booking Service)
    @PostMapping("/rooms/{id}/confirm-availability")
    public boolean confirm(@PathVariable Long id) {
        return hotelService.confirmAvailability(id);
    }

    // INTERNAL: компенсация (снятие блокировки/счетчика)
    @PostMapping("/rooms/{id}/release")
    public void release(@PathVariable Long id) {
        hotelService.releaseRoom(id);
    }
}