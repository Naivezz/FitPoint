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
public class ScheduleChangeRequestDTO {

    private Long id;
    private Long trainingClassId;
    private String requestType;
    private String reason;
    private String className;
    private String classDescription;
    private LocalDateTime requestedStartTime;
    private LocalDateTime requestedEndTime;
    private Integer requestedCapacity;
    private Long requestedRoomId;
    private String requestedRoomName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private String reviewedByName;
    private String reviewNote;
}
