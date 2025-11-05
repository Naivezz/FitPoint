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
public class PersonalTrainingSessionDTO {

    private Long id;
    private Long clientId;
    private String clientName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String sessionGoal;
    private String sessionNotes;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
