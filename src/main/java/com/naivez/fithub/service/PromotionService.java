package com.naivez.fithub.service;

import com.naivez.fithub.dto.PromotionDTO;
import com.naivez.fithub.dto.PromotionRequest;
import com.naivez.fithub.entity.Promotion;
import com.naivez.fithub.mapper.PromotionMapper;
import com.naivez.fithub.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        Promotion promotion = promotionMapper.toEntity(request);
        promotion = promotionRepository.save(promotion);
        return promotionMapper.toDto(promotion);
    }

    @Transactional
    public PromotionDTO updatePromotion(Long id, PromotionRequest request) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        promotionMapper.updateFromRequest(request, promotion);
        promotion = promotionRepository.save(promotion);
        return promotionMapper.toDto(promotion);
    }

    @Transactional
    public void deletePromotion(Long id) {
        if (!promotionRepository.existsById(id)) {
            throw new RuntimeException("Promotion not found with id: " + id);
        }
        promotionRepository.deleteById(id);
    }
}