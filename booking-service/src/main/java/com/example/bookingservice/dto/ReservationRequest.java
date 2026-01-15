package com.example.bookingservice.dto;
import lombok.Data;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationRequest {
    private Long roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
}