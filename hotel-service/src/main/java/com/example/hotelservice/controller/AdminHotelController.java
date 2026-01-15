package com.example.hotelservice.controller;

import com.example.hotelservice.entity.Hotel;
import com.example.hotelservice.entity.Room;
import com.example.hotelservice.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/hotels")
@RequiredArgsConstructor
@Tag(name = "Admin Hotel Controller", description = "Управление отелями и номерами (полный CRUD)")
public class AdminHotelController {

    private final HotelService hotelService;

    // управление отелями

    @GetMapping
    @Operation(summary = "Получить список всех отелей с их комнатами")
    public List<Hotel> getAllHotels() {
        return hotelService.getAllHotels();
    }

    @PostMapping
    @Operation(summary = "Создать новый отель")
    @ResponseStatus(HttpStatus.CREATED)
    public Hotel createHotel(@RequestBody Hotel hotel) {
        return hotelService.createHotel(hotel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить информацию об отеле")
    public Hotel updateHotel(@PathVariable Long id, @RequestBody Hotel hotelDetails) {
        return hotelService.updateHotel(id, hotelDetails);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить отель (вместе со всеми комнатами)")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }

    // управление комнатами

    @PostMapping("/{hotelId}/rooms")
    @Operation(summary = "Добавить новую комнату в отель")
    @ResponseStatus(HttpStatus.CREATED)
    public Room addRoomToHotel(@PathVariable Long hotelId, @RequestBody Room room) {
        return hotelService.addRoom(hotelId, room);
    }

    @PutMapping("/rooms/{roomId}")
    @Operation(summary = "Обновить данные комнаты (цена, доступность, тип)")
    public Room updateRoom(@PathVariable Long roomId, @RequestBody Room roomDetails) {
        return hotelService.updateRoom(roomId, roomDetails);
    }

    @DeleteMapping("/rooms/{roomId}")
    @Operation(summary = "Удалить конкретную комнату")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        hotelService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }
}