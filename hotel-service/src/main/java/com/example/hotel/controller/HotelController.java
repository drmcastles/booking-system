package com.example.hotel.controller;

import com.example.hotel.entity.Hotel;
import com.example.hotel.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {
    private final HotelService hotelService;

    @GetMapping
    public List<Hotel> getAll() {
        return hotelService.getAllHotels();
    }

    @GetMapping("/{id}")
    public Hotel getById(@PathVariable Long id) {
        return hotelService.getHotelById(id);
    }
}