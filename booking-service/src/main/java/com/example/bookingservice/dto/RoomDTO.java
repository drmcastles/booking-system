package com.example.bookingservice.dto;

import lombok.Data;

@Data
public class RoomDTO {
    private Long id;
    private String roomNumber;
    private String type;
    private int timesBooked;
    private boolean available;
    private Double price;
}