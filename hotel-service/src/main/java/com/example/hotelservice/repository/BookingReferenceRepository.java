package com.example.hotelservice.repository;

import com.example.hotelservice.entity.BookingReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingReferenceRepository extends JpaRepository<BookingReference, Long> {
}