package com.naivez.fithub.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naivez.fithub.dto.ClientProfileDTO;
import com.naivez.fithub.dto.MembershipTypeDTO;
import com.naivez.fithub.service.ClientManagementService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClientManagementControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClientManagementService clientManagementService;

    private ClientProfileDTO testClientProfile;
    private MembershipTypeDTO testMembershipType;

    @BeforeEach
    void setUp() {
        testClientProfile = ClientProfileDTO.builder()
                .id(1L)
                .email("user1@gmail.com")
                .firstName("user1")
                .lastName("user1")
                .phone("1111111111")
                .fitnessGoals("goal")
                .medicalNotes("notes")
                .build();

        testMembershipType = MembershipTypeDTO.builder()
                .type("PREMIUM")
                .durationDays(30)
                .price(new BigDecimal("99.99"))
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllClients_withAdminRole_shouldReturnOkAndClientList() throws Exception {
        ClientProfileDTO client2 = ClientProfileDTO.builder()
                .id(2L)
                .email("client2@gmail.com")
                .firstName("user2")
                .lastName("user2")
                .phone("0987654321")
                .fitnessGoals("goal1")
                .medicalNotes("notes1")
                .build();

        when(clientManagementService.getAllClients()).thenReturn(List.of(testClientProfile, client2));

        mockMvc.perform(get("/api/admin/clients"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].email").value("user1@gmail.com"))
                .andExpect(jsonPath("$[0].firstName").value("user1"))
                .andExpect(jsonPath("$[0].lastName").value("user1"))
                .andExpect(jsonPath("$[0].phone").value("1111111111"))
                .andExpect(jsonPath("$[0].fitnessGoals").value("goal"))
                .andExpect(jsonPath("$[0].medicalNotes").value("notes"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].email").value("client2@gmail.com"))
                .andExpect(jsonPath("$[1].firstName").value("user2"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllClients_whenNoClients_shouldReturnEmptyArray() throws Exception {
        when(clientManagementService.getAllClients()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/clients"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getClientById_withAdminRole_shouldReturnOkAndClient() throws Exception {
        when(clientManagementService.getClientById(1L)).thenReturn(testClientProfile);

        mockMvc.perform(get("/api/admin/clients/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("user1@gmail.com"))
                .andExpect(jsonPath("$.firstName").value("user1"))
                .andExpect(jsonPath("$.lastName").value("user1"))
                .andExpect(jsonPath("$.phone").value("1111111111"))
                .andExpect(jsonPath("$.fitnessGoals").value("goal"))
                .andExpect(jsonPath("$.medicalNotes").value("notes"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getClientById_whenClientNotFound_shouldReturnNotFound() throws Exception {
        when(clientManagementService.getClientById(999L))
                .thenThrow(new RuntimeException("Client not found with id: 999"));

        mockMvc.perform(get("/api/admin/clients/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getMembershipTypes_withAdminRole_shouldReturnOkAndMembershipTypes() throws Exception {
        MembershipTypeDTO basicType = MembershipTypeDTO.builder()
                .type("BASIC")
                .durationDays(30)
                .price(new BigDecimal("49.99"))
                .build();

        when(clientManagementService.getMembershipTypes()).thenReturn(List.of(testMembershipType, basicType));

        mockMvc.perform(get("/api/admin/clients/membership-types"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("PREMIUM"))
                .andExpect(jsonPath("$[0].durationDays").value(30))
                .andExpect(jsonPath("$[0].price").value(99.99))
                .andExpect(jsonPath("$[1].type").value("BASIC"))
                .andExpect(jsonPath("$[1].price").value(49.99));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getMembershipTypes_whenNoTypes_shouldReturnEmptyArray() throws Exception {
        when(clientManagementService.getMembershipTypes()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/clients/membership-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}