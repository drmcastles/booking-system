package com.example.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateBookingRequest {
    private Long userId;
    private Long hotelId;
    private Long roomId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
}