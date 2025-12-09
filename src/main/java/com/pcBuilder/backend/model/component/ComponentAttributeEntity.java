package com.pcBuilder.backend.model.component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    @JsonIgnore // Prevents infinite recursion in JSON serialization
    @ToString.Exclude // Prevents StackOverflow in toString()
    @EqualsAndHashCode.Exclude // Prevents issues with hashCode/equals
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

