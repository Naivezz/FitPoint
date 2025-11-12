package com.naivez.fithub.service;

import com.naivez.fithub.dto.PromotionDTO;
import com.naivez.fithub.dto.PromotionRequest;
import com.naivez.fithub.entity.Promotion;
import com.naivez.fithub.mapper.PromotionMapper;
import com.naivez.fithub.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;

    public List<PromotionDTO> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(promotionMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<PromotionDTO> getActivePromotions() {
        return promotionRepository.findActivePromotions(LocalDate.now()).stream()
                .map(promotionMapper::toDto)
                .collect(Collectors.toList());
    }

    public PromotionDTO getPromotionById(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
        return promotionMapper.toDto(promotion);
    }

    @Transactional
    public PromotionDTO createPromotion(PromotionRequest request) {
        log.info("Creating new promotion - title: {}, discount: {}%",
                request.getTitle(), request.getDiscountPercent());

        if (request.getEndDate().isBefore(request.getStartDate())) {
            log.warn("Promotion creation failed - end date before start date: {} to {}",
                    request.getStartDate(), request.getEndDate());
            throw new RuntimeException("End date must be after start date");
        }

        Promotion promotion = promotionMapper.toEntity(request);
        promotion = promotionRepository.save(promotion);
        log.info("Promotion created successfully - id: {}, title: {}, period: {} to {}",
                promotion.getId(), promotion.getTitle(),
                promotion.getStartDate(), promotion.getEndDate());

        return promotionMapper.toDto(promotion);
    }

    @Transactional
    public PromotionDTO updatePromotion(Long id, PromotionRequest request) {
        log.info("Updating promotion - id: {}, title: {}", id, request.getTitle());

        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            log.warn("Promotion update failed - end date before start date: {} to {}",
                    request.getStartDate(), request.getEndDate());
            throw new RuntimeException("End date must be after start date");
        }

        promotionMapper.updateFromRequest(request, promotion);
        promotion = promotionRepository.save(promotion);
        log.info("Promotion updated successfully - id: {}, title: {}", id, promotion.getTitle());

        return promotionMapper.toDto(promotion);
    }

    @Transactional
    public void deletePromotion(Long id) {
        log.info("Deleting promotion - id: {}", id);

        if (!promotionRepository.existsById(id)) {
            throw new RuntimeException("Promotion not found with id: " + id);
        }
        promotionRepository.deleteById(id);
        log.info("Promotion deleted successfully - id: {}", id);
    }
}