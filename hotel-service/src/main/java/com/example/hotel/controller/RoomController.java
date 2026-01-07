package com.example.hotel.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @GetMapping("/check")
    public boolean checkRoomAvailability(@RequestParam("roomId") Long roomId) {
        System.out.println(">>> HOTEL-SERVICE: Проверка доступности комнаты №" + roomId);
        // Возвращаем true, если ID меньше 1000
        return roomId != null && roomId < 1000;
    }
}