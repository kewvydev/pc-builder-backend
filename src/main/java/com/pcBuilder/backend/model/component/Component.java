package com.pcBuilder.backend.model.component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a single hardware component scraped from retailers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Component {

    private String id;
    private ComponentCategory category;
    private String name;
    private String brand;
    @Builder.Default
    private double price = 0.0d;
    private Double previousPrice;
    private String imageUrl;
    private String productUrl;
    @Builder.Default
    private boolean inStock = true;
    @Builder.Default
    private int stockUnits = 0;
    private Instant lastUpdated;
    @Builder.Default
    private Map<String, String> attributes = new HashMap<>();
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    public Optional<String> getAttribute(String key) {
        return Optional.ofNullable(attributes.get(key));
    }

    public Component putAttribute(String key, String value) {
        if (key != null && value != null) {
            attributes.put(key, value);
        }
        return this;
    }

    public Component addTag(String tag) {
        if (tag != null && !tag.isBlank()) {
            tags.add(tag.toLowerCase());
        }
        return this;
    }
}






