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
public class TrainingClassDTO {

    private Long id;
    private String name;
    private String description;
    private String trainerName;
    private String roomName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int capacity;
    private int availableSpots;
    private Double averageRating;
}
