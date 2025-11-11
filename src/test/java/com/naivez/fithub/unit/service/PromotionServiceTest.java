package com.naivez.fithub.unit.service;

import com.naivez.fithub.dto.PromotionDTO;
import com.naivez.fithub.dto.PromotionRequest;
import com.naivez.fithub.entity.Promotion;
import com.naivez.fithub.mapper.PromotionMapper;
import com.naivez.fithub.repository.PromotionRepository;
import com.naivez.fithub.service.PromotionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private PromotionMapper promotionMapper;

    @InjectMocks
    private PromotionService promotionService;

    private Promotion testPromotion;
    private PromotionDTO testPromotionDTO;
    private PromotionRequest testPromotionRequest;

    @BeforeEach
    void setUp() {
        testPromotion = Promotion.builder()
                .id(1L)
                .title("title")
                .description("description")
                .discountPercent(new BigDecimal("20.00"))
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(30))
                .build();

        testPromotionDTO = PromotionDTO.builder()
                .id(1L)
                .title("title")
                .description("description")
                .discountPercent(new BigDecimal("20.00"))
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(30))
                .build();

        testPromotionRequest = PromotionRequest.builder()
                .title("title")
                .description("description")
                .discountPercent(new BigDecimal("20.00"))
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(30))
                .build();
    }

    @Test
    void getAllPromotions_shouldReturnListOfPromotionDtos() {
        Promotion promotion2 = Promotion.builder()
                .id(2L)
                .title("title")
                .description("description")
                .discountPercent(new BigDecimal("15.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(60))
                .build();

        PromotionDTO promotionDTO2 = PromotionDTO.builder()
                .id(2L)
                .title("title1")
                .description("description1")
                .discountPercent(new BigDecimal("15.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(60))
                .build();

        when(promotionRepository.findAll()).thenReturn(List.of(testPromotion, promotion2));
        when(promotionMapper.toDto(testPromotion)).thenReturn(testPromotionDTO);
        when(promotionMapper.toDto(promotion2)).thenReturn(promotionDTO2);

        List<PromotionDTO> result = promotionService.getAllPromotions();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(testPromotionDTO, promotionDTO2);
        verify(promotionRepository).findAll();
        verify(promotionMapper, times(2)).toDto(any(Promotion.class));
    }

    @Test
    void getAllPromotions_whenNoPromotions_shouldReturnEmptyList() {
        when(promotionRepository.findAll()).thenReturn(List.of());

        List<PromotionDTO> result = promotionService.getAllPromotions();

        assertThat(result).isEmpty();
        verify(promotionRepository).findAll();
        verify(promotionMapper, never()).toDto(any());
    }

    @Test
    void getActivePromotions_shouldReturnActivePromotionsOnly() {
        LocalDate today = LocalDate.now();
        when(promotionRepository.findActivePromotions(today)).thenReturn(List.of(testPromotion));
        when(promotionMapper.toDto(testPromotion)).thenReturn(testPromotionDTO);

        List<PromotionDTO> result = promotionService.getActivePromotions();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testPromotionDTO);
        verify(promotionRepository).findActivePromotions(today);
        verify(promotionMapper).toDto(testPromotion);
    }

    @Test
    void getActivePromotions_whenNoActivePromotions_shouldReturnEmptyList() {
        LocalDate today = LocalDate.now();
        when(promotionRepository.findActivePromotions(today)).thenReturn(List.of());

        List<PromotionDTO> result = promotionService.getActivePromotions();

        assertThat(result).isEmpty();
        verify(promotionRepository).findActivePromotions(today);
        verify(promotionMapper, never()).toDto(any());
    }

    @Test
    void getPromotionById_whenPromotionExists_shouldReturnPromotionDto() {
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(testPromotion));
        when(promotionMapper.toDto(testPromotion)).thenReturn(testPromotionDTO);

        PromotionDTO result = promotionService.getPromotionById(1L);

        assertThat(result).isEqualTo(testPromotionDTO);
        verify(promotionRepository).findById(1L);
        verify(promotionMapper).toDto(testPromotion);
    }

    @Test
    void getPromotionById_whenPromotionNotFound_shouldThrowException() {
        when(promotionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> promotionService.getPromotionById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Promotion not found with id: 999");

        verify(promotionRepository).findById(999L);
        verify(promotionMapper, never()).toDto(any());
    }

    @Test
    void createPromotion_withEndDateBeforeStartDate_shouldThrowException() {
        PromotionRequest invalidRequest = PromotionRequest.builder()
                .title("promotionTest1")
                .description("Invalid dates")
                .discountPercent(new BigDecimal("10.00"))
                .startDate(LocalDate.now().plusDays(30))
                .endDate(LocalDate.now().plusDays(1))
                .build();

        assertThatThrownBy(() -> promotionService.createPromotion(invalidRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("End date must be after start date");

        verify(promotionRepository, never()).save(any());
    }

    @Test
    void updatePromotion_whenPromotionExists_shouldReturnUpdatedDto() {
        PromotionRequest updateRequest = PromotionRequest.builder()
                .title("Updated Sale")
                .description("Updated description")
                .discountPercent(new BigDecimal("25.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(45))
                .build();

        Promotion updatedPromotion = Promotion.builder()
                .id(1L)
                .title("Updated Sale")
                .description("Updated description")
                .discountPercent(new BigDecimal("25.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(45))
                .build();

        PromotionDTO updatedPromotionDTO = PromotionDTO.builder()
                .id(1L)
                .title("Updated Sale")
                .description("Updated description")
                .discountPercent(new BigDecimal("25.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(45))
                .build();

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(testPromotion));
        doNothing().when(promotionMapper).updateFromRequest(eq(updateRequest), any(Promotion.class));
        when(promotionRepository.save(any(Promotion.class))).thenReturn(updatedPromotion);
        when(promotionMapper.toDto(updatedPromotion)).thenReturn(updatedPromotionDTO);

        PromotionDTO result = promotionService.updatePromotion(1L, updateRequest);

        assertThat(result).isEqualTo(updatedPromotionDTO);
        verify(promotionRepository).findById(1L);
        verify(promotionMapper).updateFromRequest(eq(updateRequest), any(Promotion.class));
        verify(promotionRepository).save(any(Promotion.class));
        verify(promotionMapper).toDto(updatedPromotion);
    }

    @Test
    void updatePromotion_whenPromotionNotFound_shouldThrowException() {
        when(promotionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> promotionService.updatePromotion(999L, testPromotionRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Promotion not found with id: 999");

        verify(promotionRepository).findById(999L);
        verify(promotionRepository, never()).save(any());
    }

    @Test
    void updatePromotion_withInvalidDates_shouldThrowException() {
        PromotionRequest invalidUpdateRequest = PromotionRequest.builder()
                .title("Invalid Update")
                .description("Invalid dates")
                .discountPercent(new BigDecimal("10.00"))
                .startDate(LocalDate.now().plusDays(20))
                .endDate(LocalDate.now().plusDays(5))
                .build();

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(testPromotion));

        assertThatThrownBy(() -> promotionService.updatePromotion(1L, invalidUpdateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("End date must be after start date");

        verify(promotionRepository, never()).save(any());
    }

    @Test
    void deletePromotion_whenPromotionExists_shouldDeleteSuccessfully() {
        when(promotionRepository.existsById(1L)).thenReturn(true);
        doNothing().when(promotionRepository).deleteById(1L);

        promotionService.deletePromotion(1L);

        verify(promotionRepository).existsById(1L);
        verify(promotionRepository).deleteById(1L);
    }

    @Test
    void deletePromotion_whenPromotionNotFound_shouldThrowException() {
        when(promotionRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> promotionService.deletePromotion(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Promotion not found with id: 999");

        verify(promotionRepository).existsById(999L);
        verify(promotionRepository, never()).deleteById(anyLong());
    }
}