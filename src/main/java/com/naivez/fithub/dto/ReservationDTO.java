package com.naivez.fithub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {

    private Long id;
    private Long trainingClassId;
    private String className;
    private String trainerName;
    private LocalDateTime classStartTime;
    private LocalDateTime classEndTime;
    private LocalDateTime reservationDate;
    private String status;
    private Integer rating;
    private String comment;
}
