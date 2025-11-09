package com.naivez.fithub.controller;

import com.naivez.fithub.dto.PromotionDTO;
import com.naivez.fithub.dto.PromotionRequest;
import com.naivez.fithub.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PromotionDTO>> getAllPromotions() {
        List<PromotionDTO> promotions = promotionService.getAllPromotions();
        return ResponseEntity.ok(promotions);
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<List<PromotionDTO>> getActivePromotions() {
        List<PromotionDTO> promotions = promotionService.getActivePromotions();
        return ResponseEntity.ok(promotions);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromotionDTO> getPromotionById(@PathVariable Long id) {
        try {
            PromotionDTO promotion = promotionService.getPromotionById(id);
            return ResponseEntity.ok(promotion);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromotionDTO> createPromotion(@Valid @RequestBody PromotionRequest request) {
        try {
            PromotionDTO promotion = promotionService.createPromotion(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(promotion);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromotionDTO> updatePromotion(@PathVariable Long id, 
                                                        @Valid @RequestBody PromotionRequest request) {
        try {
            PromotionDTO promotion = promotionService.updatePromotion(id, request);
            return ResponseEntity.ok(promotion);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePromotion(@PathVariable Long id) {
        try {
            promotionService.deletePromotion(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}