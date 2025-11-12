package com.naivez.fithub.service;

import com.naivez.fithub.dto.CouponDTO;
import com.naivez.fithub.dto.CouponRequest;
import com.naivez.fithub.entity.Coupon;
import com.naivez.fithub.exception.CouponAlreadyExistsException;
import com.naivez.fithub.exception.EntityNotFoundException;
import com.naivez.fithub.mapper.CouponMapper;
import com.naivez.fithub.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
                .orElseThrow(() -> new EntityNotFoundException("Coupon not found with id: " + id));
        return couponMapper.toDto(coupon);
    }

    @Transactional
    public CouponDTO createCoupon(CouponRequest request) {
        log.info("Creating new coupon - code: {}", request.getCode());

        if (couponRepository.existsByCode(request.getCode())) {
            log.warn("Coupon creation failed - code already exists: {}", request.getCode());
            throw new CouponAlreadyExistsException("Coupon with code " + request.getCode() + " already exists");
        }

        Coupon coupon = couponMapper.toEntity(request);
        coupon = couponRepository.save(coupon);
        log.info("Coupon created successfully - id: {}, code: {}", coupon.getId(), coupon.getCode());

        return couponMapper.toDto(coupon);
    }

    @Transactional
    public CouponDTO updateCoupon(Long id, CouponRequest request) {
        log.info("Updating coupon - id: {}, new code: {}", id, request.getCode());

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Coupon not found with id: " + id));

        if (!coupon.getCode().equals(request.getCode()) &&
                couponRepository.existsByCode(request.getCode())) {
            log.warn("Coupon update failed - code already exists: {}", request.getCode());
            throw new CouponAlreadyExistsException("Coupon with code " + request.getCode() + " already exists");
        }

        couponMapper.updateFromRequest(request, coupon);
        coupon = couponRepository.save(coupon);
        log.info("Coupon updated successfully - id: {}, code: {}", coupon.getId(), coupon.getCode());

        return couponMapper.toDto(coupon);
    }

    @Transactional
    public void deleteCoupon(Long id) {
        log.info("Deleting coupon - id: {}", id);

        if (!couponRepository.existsById(id)) {
            throw new EntityNotFoundException("Coupon not found with id: " + id);
        }
        couponRepository.deleteById(id);
        log.info("Coupon deleted successfully - id: {}", id);
    }

    @Transactional
    public CouponDTO deactivateCoupon(Long id) {
        log.info("Deactivating coupon - id: {}", id);
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Coupon not found with id: " + id));

        coupon.setActive(false);
        coupon = couponRepository.save(coupon);
        log.info("Coupon deactivated successfully - id: {}, code: {}", coupon.getId(), coupon.getCode());

        return couponMapper.toDto(coupon);
    }
}