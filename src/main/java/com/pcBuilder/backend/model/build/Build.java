package com.pcBuilder.backend.model.build;

import com.pcBuilder.backend.model.component.Component;
import com.pcBuilder.backend.model.component.ComponentCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a full PC build assembled by the user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Build {

    private String id;
    private String name;
    @Builder.Default
    private Map<ComponentCategory, Component> selections = new EnumMap<>(ComponentCategory.class);
    @Builder.Default
    private List<String> compatibilityAlerts = new ArrayList<>();
    @Builder.Default
    private List<String> recommendations = new ArrayList<>();
    private Double estimatedPowerDraw;
    private Double budget;

    public double getTotalPrice() {
        return selections.values().stream()
                .mapToDouble(Component::getPrice)
                .sum();
    }

    public void selectComponent(Component component) {
        if (component == null || component.getCategory() == null) {
            return;
        }
        selections.put(component.getCategory(), component);
    }

    public Optional<Component> getComponent(ComponentCategory category) {
        return Optional.ofNullable(selections.get(category));
    }

    public boolean isComplete() {
        return selections.size() == ComponentCategory.values().length;
    }
}






