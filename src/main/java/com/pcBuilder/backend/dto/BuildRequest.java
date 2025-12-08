package com.pcBuilder.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BuildRequest {

    @NotBlank
    private String name;

    private Double budget;

    @NotEmpty
    @Valid
    private List<ComponentSelectionDto> components = new ArrayList<>();
}






