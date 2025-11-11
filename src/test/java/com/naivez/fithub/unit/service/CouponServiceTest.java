package com.naivez.fithub.unit.service;

import com.naivez.fithub.dto.CouponDTO;
import com.naivez.fithub.dto.CouponRequest;
import com.naivez.fithub.entity.Coupon;
import com.naivez.fithub.mapper.CouponMapper;
import com.naivez.fithub.repository.CouponRepository;
import com.naivez.fithub.service.CouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponMapper couponMapper;

    @InjectMocks
    private CouponService couponService;

    private Coupon testCoupon;
    private CouponDTO testCouponDTO;
    private CouponRequest testCouponRequest;

    @BeforeEach
    void setUp() {
        testCoupon = Coupon.builder()
                .id(1L)
                .code("SAVE10")
                .discountValue(new BigDecimal("10.00"))
                .active(true)
                .usedBy(null)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        testCouponDTO = CouponDTO.builder()
                .id(1L)
                .code("SAVE10")
                .discountValue(new BigDecimal("10.00"))
                .active(true)
                .usedById(null)
                .usedByEmail(null)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        testCouponRequest = CouponRequest.builder()
                .code("SAVE10")
                .discountValue(new BigDecimal("10.00"))
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    @Test
    void getAllCoupons_shouldReturnListOfCouponDtos() {
        Coupon coupon2 = Coupon.builder()
                .id(2L)
                .code("WELCOME20")
                .discountValue(new BigDecimal("20.00"))
                .active(true)
                .expiresAt(LocalDateTime.now().plusDays(60))
                .build();

        CouponDTO couponDTO2 = CouponDTO.builder()
                .id(2L)
                .code("WELCOME20")
                .discountValue(new BigDecimal("20.00"))
                .active(true)
                .expiresAt(LocalDateTime.now().plusDays(60))
                .build();

        when(couponRepository.findAll()).thenReturn(List.of(testCoupon, coupon2));
        when(couponMapper.toDto(testCoupon)).thenReturn(testCouponDTO);
        when(couponMapper.toDto(coupon2)).thenReturn(couponDTO2);

        List<CouponDTO> result = couponService.getAllCoupons();

        assertThat(result).hasSize(2);
        assertThat(result).contains(testCouponDTO, couponDTO2);
        verify(couponRepository).findAll();
        verify(couponMapper, times(2)).toDto(any(Coupon.class));
    }

    @Test
    void getAllCoupons_whenNoCoupons_shouldReturnEmptyList() {
        when(couponRepository.findAll()).thenReturn(List.of());

        List<CouponDTO> result = couponService.getAllCoupons();

        assertThat(result).isEmpty();
        verify(couponRepository).findAll();
        verify(couponMapper, never()).toDto(any(Coupon.class));
    }

    @Test
    void getCouponById_whenCouponExists_shouldReturnCouponDto() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));
        when(couponMapper.toDto(testCoupon)).thenReturn(testCouponDTO);

        CouponDTO result = couponService.getCouponById(1L);

        assertThat(result).isEqualTo(testCouponDTO);
        verify(couponRepository).findById(1L);
        verify(couponMapper).toDto(testCoupon);
    }

    @Test
    void getCouponById_whenCouponNotFound_shouldThrowException() {
        when(couponRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.getCouponById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Coupon not found with id: 999");

        verify(couponRepository).findById(999L);
        verify(couponMapper, never()).toDto(any(Coupon.class));
    }

    @Test
    void createCoupon_withValidRequest_shouldReturnCouponDto() {
        when(couponRepository.existsByCode("SAVE10")).thenReturn(false);
        when(couponMapper.toEntity(testCouponRequest)).thenReturn(testCoupon);
        when(couponRepository.save(testCoupon)).thenReturn(testCoupon);
        when(couponMapper.toDto(testCoupon)).thenReturn(testCouponDTO);

        CouponDTO result = couponService.createCoupon(testCouponRequest);

        assertThat(result).isEqualTo(testCouponDTO);
        verify(couponRepository).existsByCode("SAVE10");
        verify(couponMapper).toEntity(testCouponRequest);
        verify(couponRepository).save(testCoupon);
        verify(couponMapper).toDto(testCoupon);
    }

    @Test
    void createCoupon_whenCodeAlreadyExists_shouldThrowException() {
        when(couponRepository.existsByCode("SAVE10")).thenReturn(true);

        assertThatThrownBy(() -> couponService.createCoupon(testCouponRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Coupon with code SAVE10 already exists");

        verify(couponRepository).existsByCode("SAVE10");
        verify(couponMapper, never()).toEntity(any(CouponRequest.class));
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void updateCoupon_whenCouponExistsAndCodeNotChanged_shouldReturnUpdatedDto() {
        CouponRequest updateRequest = CouponRequest.builder()
                .code("SAVE10")
                .discountValue(new BigDecimal("15.00"))
                .expiresAt(LocalDateTime.now().plusDays(45))
                .build();

        Coupon updatedCoupon = Coupon.builder()
                .id(1L)
                .code("SAVE10")
                .discountValue(new BigDecimal("15.00"))
                .active(false)
                .expiresAt(LocalDateTime.now().plusDays(45))
                .build();

        CouponDTO updatedDTO = CouponDTO.builder()
                .id(1L)
                .code("SAVE10")
                .discountValue(new BigDecimal("15.00"))
                .active(false)
                .expiresAt(LocalDateTime.now().plusDays(45))
                .build();

        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));
        doNothing().when(couponMapper).updateFromRequest(eq(updateRequest), any(Coupon.class));
        when(couponRepository.save(any(Coupon.class))).thenReturn(updatedCoupon);
        when(couponMapper.toDto(updatedCoupon)).thenReturn(updatedDTO);

        CouponDTO result = couponService.updateCoupon(1L, updateRequest);

        assertThat(result).isEqualTo(updatedDTO);
        verify(couponRepository).findById(1L);
        verify(couponMapper).updateFromRequest(eq(updateRequest), any(Coupon.class));
        verify(couponRepository).save(any(Coupon.class));
        verify(couponMapper).toDto(updatedCoupon);
    }

    @Test
    void updateCoupon_whenCouponNotFound_shouldThrowException() {
        when(couponRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.updateCoupon(999L, testCouponRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Coupon not found with id: 999");

        verify(couponRepository).findById(999L);
        verify(couponRepository, never()).existsByCode(anyString());
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void updateCoupon_whenCodeChangedToExistingCode_shouldThrowException() {
        CouponRequest updateRequest = CouponRequest.builder()
                .code("NEWCODE")
                .discountValue(new BigDecimal("15.00"))
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));
        when(couponRepository.existsByCode("NEWCODE")).thenReturn(true);

        assertThatThrownBy(() -> couponService.updateCoupon(1L, updateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Coupon with code NEWCODE already exists");

        verify(couponRepository).findById(1L);
        verify(couponRepository).existsByCode("NEWCODE");
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void updateCoupon_whenCodeChangedToNewUniqueCode_shouldSucceed() {
        CouponRequest updateRequest = CouponRequest.builder()
                .code("NEWCODE")
                .discountValue(new BigDecimal("15.00"))
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        Coupon updatedCoupon = Coupon.builder()
                .id(1L)
                .code("NEWCODE")
                .discountValue(new BigDecimal("15.00"))
                .active(true)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        CouponDTO updatedDTO = CouponDTO.builder()
                .id(1L)
                .code("NEWCODE")
                .discountValue(new BigDecimal("15.00"))
                .active(true)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));
        when(couponRepository.existsByCode("NEWCODE")).thenReturn(false);
        doNothing().when(couponMapper).updateFromRequest(eq(updateRequest), any(Coupon.class));
        when(couponRepository.save(any(Coupon.class))).thenReturn(updatedCoupon);
        when(couponMapper.toDto(updatedCoupon)).thenReturn(updatedDTO);

        CouponDTO result = couponService.updateCoupon(1L, updateRequest);

        assertThat(result).isEqualTo(updatedDTO);
        verify(couponRepository).findById(1L);
        verify(couponRepository).existsByCode("NEWCODE");
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void deleteCoupon_whenCouponExists_shouldDeleteSuccessfully() {
        when(couponRepository.existsById(1L)).thenReturn(true);
        doNothing().when(couponRepository).deleteById(1L);

        couponService.deleteCoupon(1L);

        verify(couponRepository).existsById(1L);
        verify(couponRepository).deleteById(1L);
    }

    @Test
    void deleteCoupon_whenCouponNotFound_shouldThrowException() {
        when(couponRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> couponService.deleteCoupon(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Coupon not found with id: 999");

        verify(couponRepository).existsById(999L);
        verify(couponRepository, never()).deleteById(anyLong());
    }

    @Test
    void deactivateCoupon_whenCouponExists_shouldReturnDeactivatedCoupon() {
        Coupon deactivatedCoupon = Coupon.builder()
                .id(1L)
                .code("SAVE10")
                .discountValue(new BigDecimal("10.00"))
                .active(false)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        CouponDTO deactivatedDTO = CouponDTO.builder()
                .id(1L)
                .code("SAVE10")
                .discountValue(new BigDecimal("10.00"))
                .active(false)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));
        when(couponRepository.save(any(Coupon.class))).thenReturn(deactivatedCoupon);
        when(couponMapper.toDto(deactivatedCoupon)).thenReturn(deactivatedDTO);

        CouponDTO result = couponService.deactivateCoupon(1L);

        assertThat(result).isEqualTo(deactivatedDTO);
        assertThat(result.isActive()).isFalse();
        verify(couponRepository).findById(1L);
        verify(couponRepository).save(any(Coupon.class));
        verify(couponMapper).toDto(deactivatedCoupon);
    }

    @Test
    void deactivateCoupon_whenCouponNotFound_shouldThrowException() {
        when(couponRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.deactivateCoupon(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Coupon not found with id: 999");

        verify(couponRepository).findById(999L);
        verify(couponRepository, never()).save(any(Coupon.class));
    }
}