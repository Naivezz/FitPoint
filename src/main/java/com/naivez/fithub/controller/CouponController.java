package com.naivez.fithub.controller;

import com.naivez.fithub.dto.CouponDTO;
import com.naivez.fithub.dto.CouponRequest;
import com.naivez.fithub.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<List<CouponDTO>> getAllCoupons() {
        List<CouponDTO> coupons = couponService.getAllCoupons();
        return ResponseEntity.ok(coupons);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponDTO> getCouponById(@PathVariable Long id) {
        try {
            CouponDTO coupon = couponService.getCouponById(id);
            return ResponseEntity.ok(coupon);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<CouponDTO> createCoupon(@Valid @RequestBody CouponRequest request) {
        try {
            CouponDTO coupon = couponService.createCoupon(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(coupon);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CouponDTO> updateCoupon(@PathVariable Long id,
                                                  @Valid @RequestBody CouponRequest request) {
        try {
            CouponDTO coupon = couponService.updateCoupon(id, request);
            return ResponseEntity.ok(coupon);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        try {
            couponService.deleteCoupon(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<CouponDTO> deactivateCoupon(@PathVariable Long id) {
        try {
            CouponDTO coupon = couponService.deactivateCoupon(id);
            return ResponseEntity.ok(coupon);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}