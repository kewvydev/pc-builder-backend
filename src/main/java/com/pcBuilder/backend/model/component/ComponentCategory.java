package com.pcBuilder.backend.model.component;

import java.util.Arrays;

/**
 * Supported hardware categories handled by the application.
 */
public enum ComponentCategory {
    CPU("cpu", "Procesador"),
    GPU("gpu", "Tarjeta de video"),
    MOTHERBOARD("motherboard", "Placa base"),
    RAM("ram", "Memoria RAM"),
    STORAGE("storage", "Almacenamiento"),
    PSU("psu", "Fuente de poder"),
    CASE("case", "Gabinete");

    private final String slug;
    private final String displayName;

    ComponentCategory(String slug, String displayName) {
        this.slug = slug;
        this.displayName = displayName;
    }

    public String getSlug() {
        return slug;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Resolve a category from either its enum name or slug representation.
     */
    public static ComponentCategory fromValue(String value) {
        return Arrays.stream(values())
                .filter(category -> category.name().equalsIgnoreCase(value)
                        || category.slug.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown component category: " + value));
    }
}






