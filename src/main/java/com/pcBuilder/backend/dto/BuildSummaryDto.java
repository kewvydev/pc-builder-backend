package com.pcBuilder.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildSummaryDto {

    private String id;
    private String name;
    private double totalPrice;
    private Double budget;
    @Builder.Default
    private List<ComponentDto> components = Collections.emptyList();
    @Builder.Default
    private List<String> compatibilityAlerts = Collections.emptyList();
    @Builder.Default
    private List<String> recommendations = Collections.emptyList();
}






