package com.naivez.fithub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponDTO {

    private Long id;
    private String code;
    private BigDecimal discountValue;
    private boolean active;
    private Long usedById;
    private String usedByEmail;
    private LocalDateTime expiresAt;
}
