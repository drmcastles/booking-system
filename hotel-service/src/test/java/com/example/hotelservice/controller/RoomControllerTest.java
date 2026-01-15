package com.example.hotelservice.controller;

import com.example.hotelservice.entity.Room;
import com.example.hotelservice.repository.RoomRepository;
import com.example.hotelservice.repository.BookingReferenceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomRepository roomRepository;

    @MockBean
    private BookingReferenceRepository bookingRepository;

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnAvailableRooms() throws Exception {
        when(roomRepository.findByHotelId(1L)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/hotels/rooms/hotel/1/available")
                        .param("checkIn", "2026-05-01T10:00:00")
                        .param("checkOut", "2026-05-10T10:00:00"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReserveRoom() throws Exception {
        Room room = new Room();
        room.setId(1L);
        room.setBookings(new ArrayList<>());
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        String json = "{\"roomId\":1, \"checkIn\":\"2026-05-01\", \"checkOut\":\"2026-05-10\"}";

        mockMvc.perform(post("/api/hotels/rooms/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }
}