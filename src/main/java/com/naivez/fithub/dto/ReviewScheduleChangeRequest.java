package com.naivez.fithub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewScheduleChangeRequest {

    @NotBlank(message = "Status is required (APPROVED or REJECTED)")
    private String status;

    private String reviewNote;
}
