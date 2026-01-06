package com.example.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@FeignClient(name = "hotel-service")
public interface HotelClient {

    @GetMapping("/api/hotels")
    List<Object> getAllHotels();
}