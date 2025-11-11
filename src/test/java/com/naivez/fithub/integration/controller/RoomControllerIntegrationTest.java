package com.naivez.fithub.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naivez.fithub.dto.RoomDTO;
import com.naivez.fithub.dto.RoomRequest;
import com.naivez.fithub.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoomControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomService roomService;

    private RoomDTO testRoomDTO;
    private RoomRequest testRoomRequest;

    @BeforeEach
    void setUp() {
        testRoomDTO = RoomDTO.builder()
                .id(1L)
                .name("room1")
                .capacity(20)
                .equipmentList(List.of())
                .build();

        testRoomRequest = RoomRequest.builder()
                .name("room1")
                .capacity(20)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllRooms_withAdminRole_shouldReturnOkAndList() throws Exception {
        RoomDTO room2 = RoomDTO.builder()
                .id(2L)
                .name("room2")
                .capacity(30)
                .equipmentList(List.of())
                .build();

        when(roomService.getAllRooms()).thenReturn(List.of(testRoomDTO, room2));

        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("room1"))
                .andExpect(jsonPath("$[0].capacity").value(20))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("room2"));

        verify(roomService).getAllRooms();
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void getAllRooms_withClientRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRoomById_withAdminRole_shouldReturnOkAndRoom() throws Exception {
        when(roomService.getRoomById(1L)).thenReturn(testRoomDTO);

        mockMvc.perform(get("/api/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("room1"))
                .andExpect(jsonPath("$.capacity").value(20));

        verify(roomService).getRoomById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRoomById_whenNotFound_shouldReturnNotFound() throws Exception {
        when(roomService.getRoomById(999L)).thenThrow(new RuntimeException("Room not found with id: 999"));

        mockMvc.perform(get("/api/rooms/999"))
                .andExpect(status().isNotFound());

        verify(roomService).getRoomById(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRoom_withAdminRole_shouldReturnCreatedStatus() throws Exception {
        when(roomService.createRoom(any(RoomRequest.class))).thenReturn(testRoomDTO);

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRoomRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("room1"))
                .andExpect(jsonPath("$.capacity").value(20));

        verify(roomService).createRoom(any(RoomRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRoom_withAdminRole_shouldReturnOk() throws Exception {
        RoomRequest updateRequest = RoomRequest.builder()
                .name("room1")
                .capacity(25)
                .build();

        RoomDTO updatedDTO = RoomDTO.builder()
                .id(1L)
                .name("room1")
                .capacity(25)
                .equipmentList(List.of())
                .build();

        when(roomService.updateRoom(eq(1L), any(RoomRequest.class))).thenReturn(updatedDTO);

        mockMvc.perform(put("/api/rooms/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("room1"))
                .andExpect(jsonPath("$.capacity").value(25));

        verify(roomService).updateRoom(eq(1L), any(RoomRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRoom_whenNotFound_shouldReturnBadRequest() throws Exception {
        when(roomService.updateRoom(eq(999L), any(RoomRequest.class)))
                .thenThrow(new RuntimeException("Room not found"));

        mockMvc.perform(put("/api/rooms/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRoomRequest)))
                .andExpect(status().isBadRequest());

        verify(roomService).updateRoom(eq(999L), any(RoomRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteRoom_withAdminRole_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/rooms/1"))
                .andExpect(status().isNoContent());

        verify(roomService).deleteRoom(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteRoom_whenNotFound_shouldReturnBadRequest() throws Exception {
        doThrow(new RuntimeException("Room not found")).when(roomService).deleteRoom(999L);

        mockMvc.perform(delete("/api/rooms/999"))
                .andExpect(status().isBadRequest());

        verify(roomService).deleteRoom(999L);
    }
}