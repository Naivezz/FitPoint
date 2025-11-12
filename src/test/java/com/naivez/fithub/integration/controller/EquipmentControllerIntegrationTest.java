package com.naivez.fithub.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naivez.fithub.dto.EquipmentDTO;
import com.naivez.fithub.dto.EquipmentRequest;
import com.naivez.fithub.exception.EntityNotFoundException;
import com.naivez.fithub.service.EquipmentService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EquipmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EquipmentService equipmentService;

    private EquipmentDTO testEquipmentDTO;
    private EquipmentRequest equipmentRequest;

    @BeforeEach
    void setUp() {
        testEquipmentDTO = EquipmentDTO.builder()
                .id(1L)
                .name("equipment1")
                .quantity(5)
                .status("GOOD")
                .roomId(1L)
                .roomName("room2")
                .build();

        equipmentRequest = EquipmentRequest.builder()
                .name("equipment1")
                .quantity(5)
                .status("GOOD")
                .roomId(1L)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllEquipment_withAdminRole_shouldReturnEquipmentList() throws Exception {
        when(equipmentService.getAllEquipment()).thenReturn(List.of(testEquipmentDTO));

        mockMvc.perform(get("/api/equipment"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("equipment1"))
                .andExpect(jsonPath("$[0].quantity").value(5))
                .andExpect(jsonPath("$[0].status").value("GOOD"))
                .andExpect(jsonPath("$[0].roomName").value("room2"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void getAllEquipment_withClientRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/equipment"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getEquipmentById_withValidId_shouldReturnEquipment() throws Exception {
        when(equipmentService.getEquipmentById(1L)).thenReturn(testEquipmentDTO);

        mockMvc.perform(get("/api/equipment/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("equipment1"))
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getEquipmentById_withInvalidId_shouldReturnNotFound() throws Exception {
        when(equipmentService.getEquipmentById(999L))
                .thenThrow(new EntityNotFoundException("Equipment not found"));

        mockMvc.perform(get("/api/equipment/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void getEquipmentById_withClientRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/equipment/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEquipment_withValidRequest_shouldReturnCreatedEquipment() throws Exception {
        when(equipmentService.createEquipment(any(EquipmentRequest.class)))
                .thenReturn(testEquipmentDTO);

        mockMvc.perform(post("/api/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipmentRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("equipment1"))
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateEquipment_withValidRequest_shouldReturnUpdatedEquipment() throws Exception {
        EquipmentDTO updatedEquipment = EquipmentDTO.builder()
                .id(1L)
                .name("Updated")
                .quantity(8)
                .status("EXCELLENT")
                .roomId(1L)
                .roomName("room2")
                .build();

        when(equipmentService.updateEquipment(eq(1L), any(EquipmentRequest.class)))
                .thenReturn(updatedEquipment);

        mockMvc.perform(put("/api/equipment/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipmentRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.quantity").value(8));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateEquipment_withInvalidId_shouldReturnNotFound() throws Exception {
        when(equipmentService.updateEquipment(eq(999L), any(EquipmentRequest.class)))
                .thenThrow(new EntityNotFoundException("Equipment not found"));

        mockMvc.perform(put("/api/equipment/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipmentRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteEquipment_withValidId_shouldReturnNoContent() throws Exception {
        doNothing().when(equipmentService).deleteEquipment(1L);

        mockMvc.perform(delete("/api/equipment/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteEquipment_withInvalidId_shouldReturnNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Equipment not found"))
                .when(equipmentService).deleteEquipment(999L);

        mockMvc.perform(delete("/api/equipment/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getEquipmentByRoom_withValidRoomId_shouldReturnEquipmentList() throws Exception {
        when(equipmentService.getEquipmentByRoom(1L)).thenReturn(List.of(testEquipmentDTO));

        mockMvc.perform(get("/api/equipment/room/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].roomId").value(1L))
                .andExpect(jsonPath("$[0].roomName").value("room2"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getEquipmentByStatus_withValidStatus_shouldReturnEquipmentList() throws Exception {
        when(equipmentService.getEquipmentByStatus("GOOD")).thenReturn(List.of(testEquipmentDTO));

        mockMvc.perform(get("/api/equipment/status/GOOD"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("GOOD"));
    }
}