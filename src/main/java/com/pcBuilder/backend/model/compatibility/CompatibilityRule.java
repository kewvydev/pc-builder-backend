package com.pcBuilder.backend.model.compatibility;

import com.pcBuilder.backend.model.component.Component;
import com.pcBuilder.backend.model.component.ComponentCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Optional;

/**
 * Defines a simple compatibility rule comparing attributes between two
 * components.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompatibilityRule {

    private ComponentCategory primaryCategory;
    private ComponentCategory dependentCategory;
    private String primaryAttributeKey;
    private String dependentAttributeKey;
    private String description;

    public Optional<String> validate(Map<ComponentCategory, Component> selections) {
        if (selections == null) {
            return Optional.empty();
        }
        Component primary = selections.get(primaryCategory);
        Component dependent = selections.get(dependentCategory);

        if (primary == null || dependent == null) {
            return Optional.empty();
        }

        String primaryValue = extract(primary, primaryAttributeKey);
        String dependentValue = extract(dependent, dependentAttributeKey);

        if (primaryValue == null || dependentValue == null) {
            return Optional.ofNullable(description);
        }

        if (!primaryValue.equalsIgnoreCase(dependentValue)) {
            String message = description != null
                    ? description
                    : String.format("%s (%s) no es compatible con %s (%s)",
                            primaryCategory.getDisplayName(), primaryValue,
                            dependentCategory.getDisplayName(), dependentValue);
            return Optional.of(message);
        }

        return Optional.empty();
    }

    private String extract(Component component, String attributeKey) {
        if (component == null || attributeKey == null) {
            return null;
        }
        return component.getAttributes().get(attributeKey);
    }
}






