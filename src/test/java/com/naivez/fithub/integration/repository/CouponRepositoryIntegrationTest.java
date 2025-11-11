package com.naivez.fithub.integration.repository;

import com.naivez.fithub.entity.Coupon;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CouponRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CouponRepository couponRepository;

    private Coupon activeCoupon;
    private Coupon expiredCoupon;
    private Coupon inactiveCoupon;

    @BeforeEach
    void setUp() {
        activeCoupon = Coupon.builder()
                .code("SAVE20")
                .discountValue(new BigDecimal("20.00"))
                .expiresAt(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();

        expiredCoupon = Coupon.builder()
                .code("EXPIRED10")
                .discountValue(new BigDecimal("10.00"))
                .expiresAt(LocalDateTime.now().minusDays(5))
                .active(true)
                .build();

        inactiveCoupon = Coupon.builder()
                .code("INACTIVE15")
                .discountValue(new BigDecimal("15.00"))
                .expiresAt(LocalDateTime.now().plusDays(15))
                .active(false)
                .build();

        entityManager.persist(activeCoupon);
        entityManager.persist(expiredCoupon);
        entityManager.persist(inactiveCoupon);
        entityManager.flush();
    }

    @Test
    void findByCode_whenCouponExists_shouldReturnCoupon() {
        Optional<Coupon> result = couponRepository.findByCode("SAVE20");

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("SAVE20");
        assertThat(result.get().getDiscountValue()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(result.get().isActive()).isTrue();
    }

    @Test
    void findByCode_whenCouponNotExists_shouldReturnEmpty() {
        Optional<Coupon> result = couponRepository.findByCode("NONEXISTENT");

        assertThat(result).isEmpty();
    }

    @Test
    void findByCode_shouldBeCaseSensitive() {
        Optional<Coupon> result = couponRepository.findByCode("save20");

        assertThat(result).isEmpty();
    }

    @Test
    void save_shouldPersistCouponCorrectly() {
        Coupon newCoupon = Coupon.builder()
                .code("NEW50")
                .discountValue(new BigDecimal("50.00"))
                .expiresAt(LocalDateTime.now().plusDays(60))
                .active(true)
                .build();

        Coupon savedCoupon = couponRepository.save(newCoupon);

        assertThat(savedCoupon.getId()).isNotNull();
        assertThat(savedCoupon.getCode()).isEqualTo("NEW50");
        assertThat(savedCoupon.getDiscountValue()).isEqualByComparingTo(new BigDecimal("50.00"));

        entityManager.flush();
        entityManager.clear();

        Optional<Coupon> retrieved = couponRepository.findById(savedCoupon.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getCode()).isEqualTo("NEW50");
    }

    @Test
    void findAll_shouldReturnAllCoupons() {
        List<Coupon> result = couponRepository.findAll();

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Coupon::getCode)
                .containsExactlyInAnyOrder("SAVE20", "EXPIRED10", "INACTIVE15");
    }

    @Test
    void deleteById_shouldRemoveCoupon() {
        Long couponId = activeCoupon.getId();

        couponRepository.deleteById(couponId);
        entityManager.flush();

        Optional<Coupon> deleted = couponRepository.findById(couponId);
        assertThat(deleted).isEmpty();

        List<Coupon> remaining = couponRepository.findAll();
        assertThat(remaining).hasSize(2);
        assertThat(remaining).extracting(Coupon::getCode)
                .containsExactlyInAnyOrder("EXPIRED10", "INACTIVE15");
    }

    @Test
    void existsByCode_whenCouponExists_shouldReturnTrue() {
        boolean exists = couponRepository.existsByCode("SAVE20");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByCode_whenCouponNotExists_shouldReturnFalse() {
        boolean exists = couponRepository.existsByCode("NONEXISTENT");

        assertThat(exists).isFalse();
    }

    @Test
    void update_shouldModifyCouponCorrectly() {
        activeCoupon.setDiscountValue(new BigDecimal("25.00"));
        activeCoupon.setActive(false);

        Coupon updated = couponRepository.save(activeCoupon);
        entityManager.flush();
        entityManager.clear();

        Optional<Coupon> retrieved = couponRepository.findById(updated.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getDiscountValue()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(retrieved.get().isActive()).isFalse();
    }
}