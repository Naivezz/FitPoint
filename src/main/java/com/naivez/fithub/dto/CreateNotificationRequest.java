package com.naivez.fithub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {

    @NotBlank
    private String recipientEmail;

    @NotBlank
    private String message;
}

