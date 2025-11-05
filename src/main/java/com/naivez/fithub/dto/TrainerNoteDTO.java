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
public class TrainerNoteDTO {

    private Long id;
    private Long clientId;
    private String clientName;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
