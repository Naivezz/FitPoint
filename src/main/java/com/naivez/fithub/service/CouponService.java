package com.naivez.fithub.service;

import com.naivez.fithub.dto.CouponDTO;
import com.naivez.fithub.dto.CouponRequest;
import com.naivez.fithub.entity.Coupon;
import com.naivez.fithub.mapper.CouponMapper;
import com.naivez.fithub.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    public List<CouponDTO> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(couponMapper::toDto)
                .collect(Collectors.toList());
    }

    public CouponDTO getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));
        return couponMapper.toDto(coupon);
    }

    @Transactional
    public CouponDTO createCoupon(CouponRequest request) {
        if (couponRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Coupon with code " + request.getCode() + " already exists");
        }

        Coupon coupon = couponMapper.toEntity(request);
        coupon = couponRepository.save(coupon);
        return couponMapper.toDto(coupon);
    }

    @Transactional
    public CouponDTO updateCoupon(Long id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));

        if (!coupon.getCode().equals(request.getCode()) &&
                couponRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Coupon with code " + request.getCode() + " already exists");
        }

        couponMapper.updateFromRequest(request, coupon);
        coupon = couponRepository.save(coupon);
        return couponMapper.toDto(coupon);
    }

    @Transactional
    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new RuntimeException("Coupon not found with id: " + id);
        }
        couponRepository.deleteById(id);
    }

    @Transactional
    public CouponDTO deactivateCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));

        coupon.setActive(false);
        coupon = couponRepository.save(coupon);
        return couponMapper.toDto(coupon);
    }
}