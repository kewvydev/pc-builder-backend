package com.pcBuilder.backend.model.component;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * JPA Entity for component attributes (key-value pairs).
 */
@Entity
@Table(name = "component_attributes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ComponentAttributeEntity.ComponentAttributeId.class)
public class ComponentAttributeEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private ComponentEntity component;

    @Id
    @Column(name = "attribute_key", nullable = false, columnDefinition = "TEXT")
    private String attributeKey;

    @Column(name = "attribute_value", columnDefinition = "TEXT")
    private String attributeValue;

    /**
     * Composite key class for ComponentAttributeEntity.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentAttributeId implements Serializable {
        private String component;
        private String attributeKey;
    }
}

