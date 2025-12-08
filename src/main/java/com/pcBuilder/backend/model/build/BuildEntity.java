package com.pcBuilder.backend.model.build;

import com.pcBuilder.backend.model.component.Component;
import com.pcBuilder.backend.model.component.ComponentCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * JPA Entity representing a PC build stored in PostgreSQL.
 */
@Entity
@Table(name = "builds")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "budget", precision = 12, scale = 2)
    private BigDecimal budget;

    @Column(name = "estimated_power_draw", precision = 10, scale = 2)
    private BigDecimal estimatedPowerDraw;

    @Column(name = "total_price", precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "build", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BuildComponentEntity> components = new ArrayList<>();

    @OneToMany(mappedBy = "build", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BuildAlertEntity> alerts = new ArrayList<>();

    @OneToMany(mappedBy = "build", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BuildRecommendationEntity> recommendations = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Converts this entity to a domain Build object.
     */
    public Build toDomain(Map<String, Component> componentMap) {
        Map<ComponentCategory, Component> selectedComponents = new EnumMap<>(ComponentCategory.class);
        
        for (BuildComponentEntity bc : this.components) {
            ComponentCategory category = ComponentCategory.fromString(bc.getCategory());
            if (category != null && bc.getComponentId() != null) {
                Component component = componentMap.get(bc.getComponentId());
                if (component != null) {
                    selectedComponents.put(category, component);
                }
            }
        }

        Build build = Build.builder()
                .id(this.id.toString())
                .name(this.name)
                .budget(this.budget != null ? this.budget.doubleValue() : null)
                .estimatedPowerDraw(this.estimatedPowerDraw != null ? this.estimatedPowerDraw.doubleValue() : null)
                .selectedComponents(selectedComponents)
                .totalPrice(this.totalPrice != null ? this.totalPrice.doubleValue() : 0.0)
                .build();

        // Convert alerts
        for (BuildAlertEntity alert : this.alerts) {
            build.getAlerts().add(alert.getMessage());
        }

        // Convert recommendations
        for (BuildRecommendationEntity rec : this.recommendations) {
            build.getRecommendations().add(rec.getMessage());
        }

        return build;
    }

    /**
     * Creates an entity from a domain Build object.
     */
    public static BuildEntity fromDomain(Build build) {
        UUID buildId = build.getId() != null ? UUID.fromString(build.getId()) : null;
        
        BuildEntity entity = BuildEntity.builder()
                .id(buildId)
                .name(build.getName())
                .budget(build.getBudget() != null ? BigDecimal.valueOf(build.getBudget()) : null)
                .estimatedPowerDraw(build.getEstimatedPowerDraw() != null ? BigDecimal.valueOf(build.getEstimatedPowerDraw()) : null)
                .totalPrice(BigDecimal.valueOf(build.getTotalPrice()))
                .build();

        // Convert selected components
        build.getSelectedComponents().forEach((category, component) -> {
            BuildComponentEntity bc = new BuildComponentEntity();
            bc.setBuild(entity);
            bc.setCategory(category.name());
            bc.setComponentId(component.getId());
            entity.getComponents().add(bc);
        });

        // Convert alerts
        for (String alertMsg : build.getAlerts()) {
            BuildAlertEntity alert = new BuildAlertEntity();
            alert.setBuild(entity);
            alert.setMessage(alertMsg);
            entity.getAlerts().add(alert);
        }

        // Convert recommendations
        for (String recMsg : build.getRecommendations()) {
            BuildRecommendationEntity rec = new BuildRecommendationEntity();
            rec.setBuild(entity);
            rec.setMessage(recMsg);
            entity.getRecommendations().add(rec);
        }

        return entity;
    }
}

