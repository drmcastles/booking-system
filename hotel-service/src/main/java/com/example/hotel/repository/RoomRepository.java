package com.example.hotel.repository;

import com.example.hotel.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    // Сортировка по убыванию популярности (Desc)
    List<Room> findByAvailableTrueOrderByTimesBookedDescIdAsc();
    List<Room> findByAvailableTrue();
}