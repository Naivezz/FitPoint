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
public class ClassTypeRequest {

    @NotBlank(message = "Class type name is required")
    private String name;
    private String description;
}
