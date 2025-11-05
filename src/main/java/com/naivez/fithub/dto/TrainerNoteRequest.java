package com.naivez.fithub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerNoteRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotBlank(message = "Note is required")
    private String note;
}
