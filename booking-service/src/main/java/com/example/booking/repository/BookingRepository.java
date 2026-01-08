package com.example.booking.repository;

import com.example.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Поиск активных бронирований для конкретной комнаты.
     * Используется для проверки пересечения дат.
     */
    List<Booking> findByRoomIdAndStatusIn(Long roomId, List<String> statuses);

    List<Booking> findByUserId(Long userId);
}