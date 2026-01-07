package com.example.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // ID из системы безопасности (User)
    private Long roomId; // ссылка на ID комнаты из hotel-service

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private String status; // например: PENDING, CONFIRMED, CANCELLED
}