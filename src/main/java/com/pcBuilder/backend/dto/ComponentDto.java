package com.pcBuilder.backend.dto;

import com.pcBuilder.backend.model.component.ComponentCategory;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentDto {

    @NotBlank
    private String id;
    @NotBlank
    private String name;
    private String brand;
    private ComponentCategory category;
    private double price;
    private Double previousPrice;
    private boolean inStock;
    private int stockUnits;
    private String imageUrl;
    private String productUrl;
    private Map<String, String> attributes;
    private Set<String> tags;
}






