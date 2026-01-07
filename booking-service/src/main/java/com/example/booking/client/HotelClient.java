package com.example.booking.client;

import com.example.booking.dto.HotelDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hotel-service")
public interface HotelClient {

    @GetMapping("/api/hotels/{id}")
    HotelDTO getHotelById(@PathVariable("id") Long id);
}