package com.naivez.fithub.unit.mapper;

import com.naivez.fithub.dto.PromotionDTO;
import com.naivez.fithub.dto.PromotionRequest;
import com.naivez.fithub.entity.Promotion;
import com.naivez.fithub.mapper.PromotionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PromotionMapperTest {

    private PromotionMapper promotionMapper;

    @BeforeEach
    void setUp() {
        promotionMapper = Mappers.getMapper(PromotionMapper.class);
    }

    @Test
    void toDto_whenEntityHasAllFields_shouldMapCorrectly() {
        Promotion promotion = Promotion.builder()
                .id(1L)
                .title("title")
                .description("description")
                .discountPercent(new BigDecimal("30.00"))
                .startDate(LocalDate.of(2024, 6, 1))
                .endDate(LocalDate.of(2024, 8, 31))
                .build();

        PromotionDTO result = promotionMapper.toDto(promotion);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("title");
        assertThat(result.getDescription()).isEqualTo("description");
        assertThat(result.getDiscountPercent()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2024, 6, 1));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2024, 8, 31));
    }

    @Test
    void toDto_whenEntityIsNull_shouldReturnNull() {
        PromotionDTO result = promotionMapper.toDto((Promotion) null);

        assertThat(result).isNull();
    }

    @Test
    void toEntity_whenDtoHasAllFields_shouldMapCorrectly() {
        PromotionDTO dto = PromotionDTO.builder()
                .id(3L)
                .title("title")
                .description("description")
                .discountPercent(new BigDecimal("40.00"))
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 31))
                .build();

        Promotion result = promotionMapper.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getTitle()).isEqualTo("title");
        assertThat(result.getDescription()).isEqualTo("description");
        assertThat(result.getDiscountPercent()).isEqualByComparingTo(new BigDecimal("40.00"));
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2025, 1, 31));
    }

    @Test
    void toEntity_whenDtoIsNull_shouldReturnNull() {
        Promotion result = promotionMapper.toEntity((PromotionDTO) null);

        assertThat(result).isNull();
    }

    @Test
    void toEntity_whenRequestHasAllFields_shouldMapCorrectly() {
        PromotionRequest request = PromotionRequest.builder()
                .title("title")
                .description("description")
                .discountPercent(new BigDecimal("25.00"))
                .startDate(LocalDate.of(2024, 12, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .build();

        Promotion result = promotionMapper.toEntity(request);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("title");
        assertThat(result.getDescription()).isEqualTo("description");
        assertThat(result.getDiscountPercent()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2024, 12, 1));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2024, 12, 31));
        assertThat(result.getId()).isNull();
    }

    @Test
    void toEntity_whenRequestIsNull_shouldReturnNull() {
        Promotion result = promotionMapper.toEntity((PromotionRequest) null);

        assertThat(result).isNull();
    }

    @Test
    void updateFromRequest_whenCalled_shouldModifyTargetEntity() {
        Promotion existingPromotion = Promotion.builder()
                .id(4L)
                .title("Old Title")
                .description("Old description")
                .discountPercent(new BigDecimal("10.00"))
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 31))
                .build();

        PromotionRequest request = PromotionRequest.builder()
                .title("Updated Title")
                .description("Updated description")
                .discountPercent(new BigDecimal("35.00"))
                .startDate(LocalDate.of(2024, 6, 1))
                .endDate(LocalDate.of(2024, 6, 30))
                .build();

        promotionMapper.updateFromRequest(request, existingPromotion);

        assertThat(existingPromotion.getId()).isEqualTo(4L);
        assertThat(existingPromotion.getTitle()).isEqualTo("Updated Title");
        assertThat(existingPromotion.getDescription()).isEqualTo("Updated description");
        assertThat(existingPromotion.getDiscountPercent()).isEqualByComparingTo(new BigDecimal("35.00"));
        assertThat(existingPromotion.getStartDate()).isEqualTo(LocalDate.of(2024, 6, 1));
        assertThat(existingPromotion.getEndDate()).isEqualTo(LocalDate.of(2024, 6, 30));
    }

    @Test
    void updateFromRequest_withNullValues_shouldIgnoreNullFields() {
        Promotion existingPromotion = Promotion.builder()
                .id(5L)
                .title("Original Title")
                .description("Original description")
                .discountPercent(new BigDecimal("20.00"))
                .startDate(LocalDate.of(2024, 3, 1))
                .endDate(LocalDate.of(2024, 3, 31))
                .build();

        PromotionRequest request = PromotionRequest.builder()
                .title("New Title")
                .description(null)
                .discountPercent(new BigDecimal("25.00"))
                .startDate(null)
                .endDate(LocalDate.of(2024, 4, 30))
                .build();

        promotionMapper.updateFromRequest(request, existingPromotion);

        assertThat(existingPromotion.getTitle()).isEqualTo("New Title");
        assertThat(existingPromotion.getDescription()).isEqualTo("Original description");
        assertThat(existingPromotion.getDiscountPercent()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(existingPromotion.getStartDate()).isEqualTo(LocalDate.of(2024, 3, 1));
        assertThat(existingPromotion.getEndDate()).isEqualTo(LocalDate.of(2024, 4, 30));
    }
}