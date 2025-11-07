package com.naivez.fithub.mapper;

import com.naivez.fithub.dto.CouponDTO;
import com.naivez.fithub.dto.CouponRequest;
import com.naivez.fithub.entity.Coupon;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CouponMapper {

    @Mapping(target = "usedById", source = "usedBy.id")
    @Mapping(target = "usedByEmail", source = "usedBy.email")
    CouponDTO toDto(Coupon coupon);

    Coupon toEntity(CouponDTO couponDTO);

    @Mapping(target = "active", constant = "true")
    @Mapping(target = "usedBy", ignore = true)
    Coupon toEntity(CouponRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "usedBy", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateFromRequest(CouponRequest request, @MappingTarget Coupon coupon);
}
