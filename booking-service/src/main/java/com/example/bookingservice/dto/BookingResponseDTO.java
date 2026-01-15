package com.example.bookingservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingResponseDTO {
    private Long id;
    private Long userId;
    private Long roomId;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String guestName;
}