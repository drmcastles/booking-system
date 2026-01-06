package com.example.booking.controller;

import com.example.booking.client.HotelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private HotelClient hotelClient;

    @GetMapping("/test-hotels")
    public List<Object> testHotels() {
        return hotelClient.getAllHotels();
    }
}