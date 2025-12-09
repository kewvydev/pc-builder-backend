package com.pcBuilder.backend.mapper;

import com.pcBuilder.backend.dto.BuildRequest;
import com.pcBuilder.backend.dto.BuildSummaryDto;
import com.pcBuilder.backend.dto.ComponentDto;
import com.pcBuilder.backend.dto.ComponentSelectionDto;
import com.pcBuilder.backend.model.build.Build;
import com.pcBuilder.backend.model.component.ComponentCategory;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Build domain objects and DTOs.
 */
@Service
public class BuildMapper {

    private final ComponentMapper componentMapper;

    public BuildMapper(ComponentMapper componentMapper) {
        this.componentMapper = componentMapper;
    }

    /**
     * Convert domain Build to BuildSummaryDto.
     */
    public BuildSummaryDto toDto(Build build) {
        if (build == null) {
            return null;
        }

        List<ComponentDto> componentDtos = build.getSelectedComponents().values().stream()
                .map(componentMapper::toDto)
                .collect(Collectors.toList());

        return BuildSummaryDto.builder()
                .id(build.getId())
                .name(build.getName())
                .totalPrice(build.getTotalPrice())
                .budget(build.getBudget())
                .components(componentDtos)
                .compatibilityAlerts(build.getAlerts())
                .recommendations(build.getRecommendations())
                .build();
    }

    /**
     * Convert BuildRequest to domain Build.
     * Note: This only sets the basic structure. Components need to be resolved
     * separately.
     */
    public Build requestToDomain(BuildRequest request) {
        if (request == null) {
            return null;
        }

        return Build.builder()
                .name(request.getName())
                .budget(request.getBudget())
                .selectedComponents(new EnumMap<>(ComponentCategory.class))
                .build();
    }

    /**
     * Create a map of ComponentCategory to Component IDs from BuildRequest.
     * This is used to resolve components from the database.
     */
    public Map<ComponentCategory, String> extractComponentSelections(BuildRequest request) {
        if (request == null || request.getComponents() == null) {
            return Map.of();
        }

        Map<ComponentCategory, String> selections = new EnumMap<>(ComponentCategory.class);

        for (ComponentSelectionDto selection : request.getComponents()) {
            ComponentCategory category = ComponentCategory.fromString(selection.getCategory());
            if (category != null) {
                selections.put(category, selection.getComponentId());
            }
        }

        return selections;
    }

    /**
     * Convert list of Builds to list of BuildSummaryDtos.
     */
    public List<BuildSummaryDto> toDtoList(List<Build> builds) {
        if (builds == null) {
            return List.of();
        }

        return builds.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
