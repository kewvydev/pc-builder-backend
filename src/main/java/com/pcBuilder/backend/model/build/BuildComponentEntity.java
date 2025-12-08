package com.pcBuilder.backend.model.build;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * JPA Entity representing a component selection within a build.
 */
@Entity
@Table(name = "build_components")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(BuildComponentEntity.BuildComponentId.class)
public class BuildComponentEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "build_id", nullable = false)
    private BuildEntity build;

    @Id
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "component_id", columnDefinition = "TEXT")
    private String componentId;

    /**
     * Composite key class for BuildComponentEntity.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BuildComponentId implements Serializable {
        private UUID build;
        private String category;
    }
}

