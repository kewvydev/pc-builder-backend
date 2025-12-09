package com.pcBuilder.backend.model.component;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * Supported hardware categories handled by the application.
 */
public enum ComponentCategory {
    CPU("cpu", "Procesador"),
    GPU("gpu", "Tarjeta de video"),
    MONITOR("monitor", "Monitor"),
    KEYBOARD("keyboard", "Teclado"),
    MOUSE("mouse", "Mouse"),
    SPEAKERS("speakers", "Parlantes"),
    OS("os", "Sistema operativo"),
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

    /**
     * Resolve a category from either its enum name or slug representation.
     * Returns null if the value is invalid instead of throwing an exception.
     */
    public static ComponentCategory fromString(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(category -> category.name().equalsIgnoreCase(value)
                        || category.slug.equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }

    /**
     * Categories that are considered essential for a PC build.
     * Optional items (e.g., monitor) are intentionally excluded.
     */
    public static EnumSet<ComponentCategory> requiredForBuild() {
        return EnumSet.of(CPU, GPU, MOTHERBOARD, RAM, STORAGE, PSU, CASE);
    }
}
