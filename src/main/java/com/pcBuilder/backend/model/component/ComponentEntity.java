package com.pcBuilder.backend.model.component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JPA Entity representing a hardware component stored in PostgreSQL.
 * 
 * PERFORMANCE NOTES:
 * - attributes and tags are LAZY loaded to avoid N+1 queries
 * - Use ComponentListDto for listings (avoids loading collections)
 * - For full details, use separate queries for attributes and tags to avoid Cartesian Product
 */
@Entity
@Table(name = "components", indexes = {
    @Index(name = "idx_components_category", columnList = "category"),
    @Index(name = "idx_components_brand", columnList = "brand"),
    @Index(name = "idx_components_price", columnList = "price"),
    @Index(name = "idx_components_in_stock", columnList = "in_stock")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ComponentEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String name;

    @Column(name = "brand", columnDefinition = "TEXT")
    private String brand;

    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "previous_price", precision = 12, scale = 2)
    private BigDecimal previousPrice;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "product_url", columnDefinition = "TEXT")
    private String productUrl;

    @Column(name = "in_stock", nullable = false)
    @Builder.Default
    private Boolean inStock = true;

    @Column(name = "stock_units", nullable = false)
    @Builder.Default
    private Integer stockUnits = 0;

    @Column(name = "last_updated")
    private Instant lastUpdated;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude // Prevents lazy loading in toString()
    @EqualsAndHashCode.Exclude // Prevents lazy loading in hashCode/equals
    private List<ComponentAttributeEntity> attributes = new ArrayList<>();

    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude // Prevents lazy loading in toString()
    @EqualsAndHashCode.Exclude // Prevents lazy loading in hashCode/equals
    private Set<ComponentTagEntity> tags = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (lastUpdated == null) {
            lastUpdated = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Converts this entity to a domain Component object.
     */
    public Component toDomain() {
        Component.ComponentBuilder builder = Component.builder()
                .id(this.id)
                .category(ComponentCategory.fromString(this.category))
                .name(this.name)
                .brand(this.brand)
                .price(this.price != null ? this.price.doubleValue() : 0.0)
                .previousPrice(this.previousPrice != null ? this.previousPrice.doubleValue() : null)
                .imageUrl(this.imageUrl)
                .productUrl(this.productUrl)
                .inStock(this.inStock)
                .stockUnits(this.stockUnits)
                .lastUpdated(this.lastUpdated);

        Component component = builder.build();

        // Convert attributes
        for (ComponentAttributeEntity attr : this.attributes) {
            component.putAttribute(attr.getAttributeKey(), attr.getAttributeValue());
        }

        // Convert tags
        for (ComponentTagEntity tag : this.tags) {
            component.addTag(tag.getTag());
        }

        return component;
    }

    /**
     * Creates an entity from a domain Component object.
     */
    public static ComponentEntity fromDomain(Component component) {
        ComponentEntity entity = ComponentEntity.builder()
                .id(component.getId())
                .category(component.getCategory() != null ? component.getCategory().name() : null)
                .name(component.getName())
                .brand(component.getBrand())
                .price(BigDecimal.valueOf(component.getPrice()))
                .previousPrice(component.getPreviousPrice() != null ? BigDecimal.valueOf(component.getPreviousPrice()) : null)
                .imageUrl(component.getImageUrl())
                .productUrl(component.getProductUrl())
                .inStock(component.isInStock())
                .stockUnits(component.getStockUnits())
                .lastUpdated(component.getLastUpdated())
                .build();

        // Convert attributes
        component.getAttributes().forEach((key, value) -> {
            ComponentAttributeEntity attr = new ComponentAttributeEntity();
            attr.setComponent(entity);
            attr.setAttributeKey(key);
            attr.setAttributeValue(value);
            entity.getAttributes().add(attr);
        });

        // Convert tags
        component.getTags().forEach(tag -> {
            ComponentTagEntity tagEntity = new ComponentTagEntity();
            tagEntity.setComponent(entity);
            tagEntity.setTag(tag);
            entity.getTags().add(tagEntity);
        });

        return entity;
    }
}

