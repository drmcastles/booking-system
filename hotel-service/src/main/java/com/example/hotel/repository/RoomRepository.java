package com.example.hotel.repository;

import com.example.hotel.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    // Получение комнат, отсортированных по возрастанию timesBooked (требование ТЗ)
    List<Room> findByAvailableTrueOrderByTimesBookedAscIdAsc();
}