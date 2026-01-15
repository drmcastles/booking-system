package com.example.bookingservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
public class BookingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long hotelId;
    private Long roomId;

    @Column(name = "booking_check_in")
    private LocalDateTime checkInDate;

    @Column(name = "booking_check_out")
    private LocalDateTime checkOutDate;

    private String status;
}