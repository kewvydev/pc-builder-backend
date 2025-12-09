package com.pcBuilder.backend.model.build;

import com.pcBuilder.backend.model.component.Component;
import com.pcBuilder.backend.model.component.ComponentCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
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
    private Map<ComponentCategory, Component> selectedComponents = new EnumMap<>(ComponentCategory.class);
    @Builder.Default
    private List<String> alerts = new ArrayList<>();
    @Builder.Default
    private List<String> recommendations = new ArrayList<>();
    private Double estimatedPowerDraw;
    private Double budget;
    private Double totalPrice;
    private String imageUrl;
    private String userEmail;
    private String userNickname;
    @Builder.Default
    private Boolean recommended = Boolean.FALSE;
    private Instant createdAt;
    private Instant updatedAt;

    public double getTotalPrice() {
        if (totalPrice != null) {
            return totalPrice;
        }
        return selectedComponents.values().stream()
                .mapToDouble(Component::getPrice)
                .sum();
    }

    public void selectComponent(Component component) {
        if (component == null || component.getCategory() == null) {
            return;
        }
        selectedComponents.put(component.getCategory(), component);
    }

    public Optional<Component> getComponent(ComponentCategory category) {
        return Optional.ofNullable(selectedComponents.get(category));
    }

    public boolean isComplete() {
        EnumSet<ComponentCategory> required = ComponentCategory.requiredForBuild();
        return selectedComponents.keySet().containsAll(required);
    }

    // Convenience methods for backward compatibility
    public Map<ComponentCategory, Component> getSelections() {
        return selectedComponents;
    }

    public List<String> getCompatibilityAlerts() {
        return alerts;
    }
}
