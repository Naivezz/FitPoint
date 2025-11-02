package com.naivez.fithub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipDTO {

    private Long id;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal price;
    private boolean active;
    private long daysRemaining;
}
