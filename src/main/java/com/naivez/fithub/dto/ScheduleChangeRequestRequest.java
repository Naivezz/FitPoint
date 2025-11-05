package com.naivez.fithub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleChangeRequestRequest {

    private Long trainingClassId;

    @NotBlank(message = "Request type is required (ADD, MODIFY, CANCEL)")
    private String requestType;

    private String reason;
    private String className;
    private String classDescription;
    private LocalDateTime requestedStartTime;
    private LocalDateTime requestedEndTime;
    private Integer requestedCapacity;
    private Long requestedRoomId;
}
