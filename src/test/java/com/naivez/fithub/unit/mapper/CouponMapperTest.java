package com.naivez.fithub.unit.mapper;

import com.naivez.fithub.dto.CouponDTO;
import com.naivez.fithub.dto.CouponRequest;
import com.naivez.fithub.entity.Coupon;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.CouponMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CouponMapperTest {

    private CouponMapper couponMapper;

    @BeforeEach
    void setUp() {
        couponMapper = Mappers.getMapper(CouponMapper.class);
    }

    @Test
    void toDto_whenEntityHasBasicFields_shouldMapAllFields() {
        User user = User.builder()
                .id(1L)
                .email("user@gmail.com")
                .build();

        Coupon coupon = Coupon.builder()
                .id(1L)
                .code("SAVE10")
                .discountValue(new BigDecimal("10.00"))
                .active(true)
                .usedBy(user)
                .expiresAt(LocalDateTime.of(2025, 12, 31, 23, 59, 59))
                .build();

        CouponDTO result = couponMapper.toDto(coupon);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("SAVE10");
        assertThat(result.getDiscountValue()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(result.isActive()).isTrue();
        assertThat(result.getUsedById()).isEqualTo(1L);
        assertThat(result.getUsedByEmail()).isEqualTo("user@gmail.com");
        assertThat(result.getExpiresAt()).isEqualTo(LocalDateTime.of(2025, 12, 31, 23, 59, 59));
    }

    @Test
    void toDto_whenEntityIsNull_shouldReturnNull() {
        CouponDTO result = couponMapper.toDto((Coupon) null);

        assertThat(result).isNull();
    }

    @Test
    void toDto_whenUserIsNull_shouldMapOtherFields() {
        Coupon coupon = Coupon.builder()
                .id(2L)
                .code("WELCOME20")
                .discountValue(new BigDecimal("20.00"))
                .active(false)
                .usedBy(null)
                .expiresAt(LocalDateTime.of(2025, 6, 15, 0, 0, 0))
                .build();

        CouponDTO result = couponMapper.toDto(coupon);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getCode()).isEqualTo("WELCOME20");
        assertThat(result.getDiscountValue()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(result.isActive()).isFalse();
        assertThat(result.getUsedById()).isNull();
        assertThat(result.getUsedByEmail()).isNull();
        assertThat(result.getExpiresAt()).isEqualTo(LocalDateTime.of(2025, 6, 15, 0, 0, 0));
    }

    @Test
    void toEntity_whenDtoHasAllFields_shouldMapCorrectly() {
        CouponDTO dto = CouponDTO.builder()
                .id(1L)
                .code("DISCOUNT15")
                .discountValue(new BigDecimal("15.00"))
                .active(true)
                .usedById(2L)
                .usedByEmail("user@gmail.com")
                .expiresAt(LocalDateTime.of(2025, 9, 30, 23, 59, 59))
                .build();

        Coupon result = couponMapper.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("DISCOUNT15");
        assertThat(result.getDiscountValue()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(result.isActive()).isTrue();
        assertThat(result.getExpiresAt()).isEqualTo(LocalDateTime.of(2025, 9, 30, 23, 59, 59));
    }

    @Test
    void toEntity_whenDtoIsNull_shouldReturnNull() {
        Coupon result = couponMapper.toEntity((CouponDTO) null);

        assertThat(result).isNull();
    }

    @Test
    void toEntity_whenRequestHasAllFields_shouldMapCorrectly() {
        CouponRequest request = CouponRequest.builder()
                .code("NEWUSER25")
                .discountValue(new BigDecimal("25.00"))
                .expiresAt(LocalDateTime.of(2026, 1, 1, 0, 0, 0))
                .build();

        Coupon result = couponMapper.toEntity(request);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("NEWUSER25");
        assertThat(result.getDiscountValue()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(result.isActive()).isTrue();
        assertThat(result.getExpiresAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0, 0));
        assertThat(result.getId()).isNull();
        assertThat(result.getUsedBy()).isNull();
    }

    @Test
    void toEntity_whenRequestIsNull_shouldReturnNull() {
        Coupon result = couponMapper.toEntity((CouponRequest) null);

        assertThat(result).isNull();
    }

    @Test
    void updateFromRequest_whenCalled_shouldModifyTargetEntity() {
        User originalUser = User.builder()
                .id(1L)
                .email("user1@gmail.com")
                .build();

        Coupon existingCoupon = Coupon.builder()
                .id(1L)
                .code("OLDCODE")
                .discountValue(new BigDecimal("5.00"))
                .active(false)
                .usedBy(originalUser)
                .expiresAt(LocalDateTime.of(2024, 12, 31, 23, 59, 59))
                .build();

        CouponRequest request = CouponRequest.builder()
                .code("NEWCODE")
                .discountValue(new BigDecimal("30.00"))
                .expiresAt(LocalDateTime.of(2026, 6, 30, 23, 59, 59))
                .build();

        couponMapper.updateFromRequest(request, existingCoupon);

        assertThat(existingCoupon.getId()).isEqualTo(1L);
        assertThat(existingCoupon.getCode()).isEqualTo("NEWCODE");
        assertThat(existingCoupon.getDiscountValue()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(existingCoupon.getExpiresAt()).isEqualTo(LocalDateTime.of(2026, 6, 30, 23, 59, 59));
        assertThat(existingCoupon.getUsedBy()).isEqualTo(originalUser);
    }

    @Test
    void updateFromRequest_whenRequestIsNull_shouldNotModifyEntity() {
        Coupon existingCoupon = Coupon.builder()
                .id(1L)
                .code("ORIGINAL")
                .discountValue(new BigDecimal("10.00"))
                .active(true)
                .expiresAt(LocalDateTime.of(2025, 12, 31, 23, 59, 59))
                .build();

        couponMapper.updateFromRequest(null, existingCoupon);

        assertThat(existingCoupon.getCode()).isEqualTo("ORIGINAL");
        assertThat(existingCoupon.getDiscountValue()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(existingCoupon.isActive()).isTrue();
    }

    @Test
    void toDto_withZeroDiscountValue_shouldMapCorrectly() {
        Coupon coupon = Coupon.builder()
                .id(3L)
                .code("FREE")
                .discountValue(BigDecimal.ZERO)
                .active(true)
                .usedBy(null)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        CouponDTO result = couponMapper.toDto(coupon);

        assertThat(result).isNotNull();
        assertThat(result.getDiscountValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getCode()).isEqualTo("FREE");
    }
}