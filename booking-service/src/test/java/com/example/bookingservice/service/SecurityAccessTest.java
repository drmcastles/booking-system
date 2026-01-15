package com.example.bookingservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("USER не должен иметь доступ к получению всех броней")
    @WithMockUser(authorities = "USER")
    void userAccessDenied() throws Exception {
        mockMvc.perform(get("/api/bookings/all"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN должен иметь полный доступ к CRUD")
    @WithMockUser(authorities = "ADMIN")
    void adminAccessAllowed() throws Exception {
        mockMvc.perform(get("/api/bookings/all"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Анонимный пользователь должен получить отказ доступа")
    void anonymousAccessDenied() throws Exception {
        mockMvc.perform(get("/api/bookings/all"))
                .andExpect(status().isForbidden());
    }
}