package com.pcBuilder.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ComponentSelectionDto {

    @NotBlank
    private String category;

    @NotBlank
    private String componentId;
}






