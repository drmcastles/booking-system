package com.example.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "hotel-service")
public interface HotelClient {

    @PostMapping("/api/rooms/{id}/confirm-availability")
    boolean confirmAvailability(@PathVariable("id") Long id);

    @PostMapping("/api/rooms/{id}/release")
    void releaseRoom(@PathVariable("id") Long id);
}