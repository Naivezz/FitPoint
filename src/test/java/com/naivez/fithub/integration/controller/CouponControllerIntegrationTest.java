package com.naivez.fithub.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naivez.fithub.dto.CouponDTO;
import com.naivez.fithub.dto.CouponRequest;
import com.naivez.fithub.service.CouponService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CouponControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CouponService couponService;

    private CouponDTO testCouponDTO;
    private CouponRequest testCouponRequest;

    @BeforeEach
    void setUp() {
        testCouponDTO = CouponDTO.builder()
                .id(1L)
                .code("COUPON1")
                .discountValue(new BigDecimal("20.00"))
                .active(true)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        testCouponRequest = CouponRequest.builder()
                .code("COUPON1")
                .discountValue(new BigDecimal("20.00"))
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCoupons_withAdminRole_shouldReturnOkAndCouponList() throws Exception {
        CouponDTO coupon2 = CouponDTO.builder()
                .id(2L)
                .code("COUPON2")
                .discountValue(new BigDecimal("15.00"))
                .active(true)
                .expiresAt(LocalDateTime.now().plusDays(60))
                .build();

        when(couponService.getAllCoupons()).thenReturn(List.of(testCouponDTO, coupon2));

        mockMvc.perform(get("/api/coupons"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].code").value("COUPON1"))
                .andExpect(jsonPath("$[0].discountValue").value(20.00))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].code").value("COUPON2"))
                .andExpect(jsonPath("$[1].discountValue").value(15.00));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCouponById_withAdminRole_shouldReturnOkAndCoupon() throws Exception {
        when(couponService.getCouponById(1L)).thenReturn(testCouponDTO);

        mockMvc.perform(get("/api/coupons/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.code").value("COUPON1"))
                .andExpect(jsonPath("$.discountValue").value(20.00));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCouponById_whenCouponNotFound_shouldReturnNotFound() throws Exception {
        when(couponService.getCouponById(999L))
                .thenThrow(new RuntimeException("Coupon not found with id: 999"));

        mockMvc.perform(get("/api/coupons/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCoupon_withAdminRole_shouldReturnCreatedAndCoupon() throws Exception {
        when(couponService.createCoupon(any(CouponRequest.class))).thenReturn(testCouponDTO);

        mockMvc.perform(post("/api/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCouponRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.code").value("COUPON1"))
                .andExpect(jsonPath("$.discountValue").value(20.00));

        verify(couponService).createCoupon(any(CouponRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCoupon_withEmptyRequestBody_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCoupon_withAdminRole_shouldReturnOkAndUpdatedCoupon() throws Exception {
        CouponRequest updateRequest = CouponRequest.builder()
                .code("UPDATED2024")
                .discountValue(new BigDecimal("25.00"))
                .expiresAt(LocalDateTime.now().plusDays(45))
                .build();

        CouponDTO updatedCoupon = CouponDTO.builder()
                .id(1L)
                .code("UPDATED2024")
                .discountValue(new BigDecimal("25.00"))
                .active(true)
                .expiresAt(LocalDateTime.now().plusDays(45))
                .build();

        when(couponService.updateCoupon(eq(1L), any(CouponRequest.class))).thenReturn(updatedCoupon);

        mockMvc.perform(put("/api/coupons/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("UPDATED2024"))
                .andExpect(jsonPath("$.discountValue").value(25.00));

        verify(couponService).updateCoupon(eq(1L), any(CouponRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCoupon_whenCouponNotFound_shouldReturnBadRequest() throws Exception {
        when(couponService.updateCoupon(eq(999L), any(CouponRequest.class)))
                .thenThrow(new RuntimeException("Coupon not found with id: 999"));

        mockMvc.perform(put("/api/coupons/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCouponRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCoupon_withAdminRole_shouldReturnNoContent() throws Exception {
        doNothing().when(couponService).deleteCoupon(1L);

        mockMvc.perform(delete("/api/coupons/1"))
                .andExpect(status().isNoContent());

        verify(couponService).deleteCoupon(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCoupon_whenCouponNotFound_shouldReturnBadRequest() throws Exception {
        doThrow(new RuntimeException("Coupon not found with id: 999"))
                .when(couponService).deleteCoupon(999L);

        mockMvc.perform(delete("/api/coupons/999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateCoupon_withAdminRole_shouldReturnOkAndDeactivatedCoupon() throws Exception {
        CouponDTO deactivatedCoupon = CouponDTO.builder()
                .id(1L)
                .code("COUPON1")
                .discountValue(new BigDecimal("20.00"))
                .active(false)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        when(couponService.deactivateCoupon(1L)).thenReturn(deactivatedCoupon);

        mockMvc.perform(put("/api/coupons/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.active").value(false));

        verify(couponService).deactivateCoupon(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateCoupon_whenCouponNotFound_shouldReturnBadRequest() throws Exception {
        when(couponService.deactivateCoupon(999L))
                .thenThrow(new RuntimeException("Coupon not found with id: 999"));

        mockMvc.perform(put("/api/coupons/999/deactivate"))
                .andExpect(status().isBadRequest());
    }
}