package com.naivez.fithub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientProfileDTO {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String fitnessGoals;
    private String medicalNotes;
}
