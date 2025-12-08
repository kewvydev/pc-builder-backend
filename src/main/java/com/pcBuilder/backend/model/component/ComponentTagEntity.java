package com.pcBuilder.backend.model.component;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * JPA Entity for component tags.
 */
@Entity
@Table(name = "component_tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ComponentTagEntity.ComponentTagId.class)
public class ComponentTagEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private ComponentEntity component;

    @Id
    @Column(name = "normalized_tag", nullable = false, columnDefinition = "TEXT", insertable = false, updatable = false)
    private String normalizedTag;

    @Column(name = "tag", nullable = false, columnDefinition = "TEXT")
    private String tag;

    /**
     * Composite key class for ComponentTagEntity.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentTagId implements Serializable {
        private String component;
        private String normalizedTag;
    }
}

