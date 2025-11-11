package com.naivez.fithub.unit.mapper;

import com.naivez.fithub.dto.MembershipDTO;
import com.naivez.fithub.entity.Membership;
import com.naivez.fithub.entity.User;
import com.naivez.fithub.mapper.MembershipMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class MembershipMapperTest {

    private MembershipMapper membershipMapper;

    @BeforeEach
    void setUp() {
        membershipMapper = Mappers.getMapper(MembershipMapper.class);
    }

    @Test
    void toDto_whenEntityHasBasicFields_shouldMapAllFields() {
        User user = User.builder()
                .id(1L)
                .email("user1@gmail.com")
                .build();

        Membership membership = Membership.builder()
                .id(1L)
                .user(user)
                .type("MONTHLY")
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusDays(20))
                .price(new BigDecimal("99.99"))
                .active(true)
                .build();

        MembershipDTO result = membershipMapper.toDto(membership);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getType()).isEqualTo("MONTHLY");
        assertThat(result.getStartDate()).isEqualTo(membership.getStartDate());
        assertThat(result.getEndDate()).isEqualTo(membership.getEndDate());
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(result.isActive()).isTrue();
        assertThat(result.getDaysRemaining()).isEqualTo(20L);
    }

    @Test
    void toDto_whenEntityIsNull_shouldReturnNull() {
        MembershipDTO result = membershipMapper.toDto((Membership) null);

        assertThat(result).isNull();
    }

    @Test
    void toDto_whenMembershipExpired_shouldCalculateZeroDaysRemaining() {
        User user = User.builder()
                .id(2L)
                .email("expired@gmail.com")
                .build();

        Membership expiredMembership = Membership.builder()
                .id(2L)
                .user(user)
                .type("QUARTERLY")
                .startDate(LocalDate.now().minusDays(100))
                .endDate(LocalDate.now().minusDays(10))
                .price(new BigDecimal("249.99"))
                .active(false)
                .build();

        MembershipDTO result = membershipMapper.toDto(expiredMembership);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getType()).isEqualTo("QUARTERLY");
        assertThat(result.isActive()).isFalse();
        assertThat(result.getDaysRemaining()).isEqualTo(0L);
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("249.99"));
    }

    @Test
    void toEntity_whenDtoHasAllFields_shouldMapCorrectly() {
        MembershipDTO dto = MembershipDTO.builder()
                .id(1L)
                .type("MONTHLY")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 2, 1))
                .price(new BigDecimal("99.99"))
                .active(true)
                .daysRemaining(30L)
                .build();

        Membership result = membershipMapper.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getType()).isEqualTo("MONTHLY");
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2025, 2, 1));
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void toEntity_whenDtoIsNull_shouldReturnNull() {
        Membership result = membershipMapper.toEntity((MembershipDTO) null);

        assertThat(result).isNull();
    }

    @Test
    void calculateDaysRemaining_whenEndDateInFuture_shouldReturnPositiveValue() {
        LocalDate futureDate = LocalDate.now().plusDays(15);

        long result = membershipMapper.calculateDaysRemaining(futureDate);

        assertThat(result).isEqualTo(15L);
    }

    @Test
    void calculateDaysRemaining_whenEndDateInPast_shouldReturnZero() {
        LocalDate pastDate = LocalDate.now().minusDays(5);

        long result = membershipMapper.calculateDaysRemaining(pastDate);

        assertThat(result).isEqualTo(0L);
    }

    @Test
    void calculateDaysRemaining_whenEndDateIsToday_shouldReturnZero() {
        LocalDate today = LocalDate.now();

        long result = membershipMapper.calculateDaysRemaining(today);

        assertThat(result).isEqualTo(0L);
    }
}