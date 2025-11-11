package com.naivez.fithub.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naivez.fithub.dto.PromotionDTO;
import com.naivez.fithub.dto.PromotionRequest;
import com.naivez.fithub.service.PromotionService;
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
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PromotionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PromotionService promotionService;

    private PromotionDTO testPromotionDTO;
    private PromotionRequest testPromotionRequest;

    @BeforeEach
    void setUp() {
        testPromotionDTO = PromotionDTO.builder()
                .id(1L)
                .title("promotionTest1")
                .description("test promotion description")
                .discountPercent(new BigDecimal("20.00"))
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusDays(25))
                .build();

        testPromotionRequest = PromotionRequest.builder()
                .title("promotionTest1")
                .description("test promotion description")
                .discountPercent(new BigDecimal("20.00"))
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusDays(25))
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllPromotions_withAdminRole_shouldReturnOkAndPromotionList() throws Exception {
        when(promotionService.getAllPromotions()).thenReturn(List.of(testPromotionDTO));

        mockMvc.perform(get("/api/promotions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("promotionTest1"))
                .andExpect(jsonPath("$[0].discountPercent").value(20.00));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void getActivePromotions_withClientRole_shouldReturnOkAndActivePromotions() throws Exception {
        when(promotionService.getActivePromotions()).thenReturn(List.of(testPromotionDTO));

        mockMvc.perform(get("/api/promotions/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void getActivePromotions_whenNoActivePromotions_shouldReturnEmptyArray() throws Exception {
        when(promotionService.getActivePromotions()).thenReturn(List.of());

        mockMvc.perform(get("/api/promotions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPromotionById_withAdminRole_shouldReturnOkAndPromotion() throws Exception {
        when(promotionService.getPromotionById(1L)).thenReturn(testPromotionDTO);

        mockMvc.perform(get("/api/promotions/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("promotionTest1"))
                .andExpect(jsonPath("$.discountPercent").value(20.00));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPromotionById_whenPromotionNotFound_shouldReturnNotFound() throws Exception {
        when(promotionService.getPromotionById(999L))
                .thenThrow(new RuntimeException("Promotion not found with id: 999"));

        mockMvc.perform(get("/api/promotions/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPromotion_withAdminRole_shouldReturnCreatedAndPromotion() throws Exception {
        when(promotionService.createPromotion(any(PromotionRequest.class))).thenReturn(testPromotionDTO);

        mockMvc.perform(post("/api/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPromotionRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("promotionTest1"))
                .andExpect(jsonPath("$.description").value("test promotion description"))
                .andExpect(jsonPath("$.discountPercent").value(20.00));

        verify(promotionService).createPromotion(any(PromotionRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePromotion_withAdminRole_shouldReturnOkAndPromotion() throws Exception {
        when(promotionService.updatePromotion(eq(1L), any(PromotionRequest.class)))
                .thenReturn(testPromotionDTO);

        mockMvc.perform(put("/api/promotions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPromotionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePromotion_whenPromotionNotFound_shouldReturnBadRequest() throws Exception {
        when(promotionService.updatePromotion(eq(999L), any(PromotionRequest.class)))
                .thenThrow(new RuntimeException("Promotion not found with id: 999"));

        mockMvc.perform(put("/api/promotions/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPromotionRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePromotion_withAdminRole_shouldReturnNoContent() throws Exception {
        doNothing().when(promotionService).deletePromotion(1L);

        mockMvc.perform(delete("/api/promotions/1"))
                .andExpect(status().isNoContent());

        verify(promotionService).deletePromotion(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePromotion_whenPromotionNotFound_shouldReturnBadRequest() throws Exception {
        doThrow(new RuntimeException("Promotion not found with id: 999"))
                .when(promotionService).deletePromotion(999L);

        mockMvc.perform(delete("/api/promotions/999"))
                .andExpect(status().isBadRequest());
    }
}