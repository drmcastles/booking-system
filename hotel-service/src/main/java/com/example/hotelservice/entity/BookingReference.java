package com.example.hotelservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "booking_references")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString // Используем ToString без автоматических связей
public class BookingReference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate checkIn;
    private LocalDate checkOut;
    private String status; // PENDING, CONFIRMED, CANCELLED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    @JsonBackReference
    @ToString.Exclude
    private Room room;
}