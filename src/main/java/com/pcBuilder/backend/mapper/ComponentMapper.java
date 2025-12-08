package com.pcBuilder.backend.mapper;

import com.pcBuilder.backend.dto.ComponentDto;
import com.pcBuilder.backend.model.component.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Component domain objects and ComponentDto.
 */
@org.springframework.stereotype.Component
public class ComponentMapper {

    /**
     * Convert domain Component to ComponentDto.
     */
    public ComponentDto toDto(Component component) {
        if (component == null) {
            return null;
        }

        return ComponentDto.builder()
                .id(component.getId())
                .name(component.getName())
                .brand(component.getBrand())
                .category(component.getCategory())
                .price(component.getPrice())
                .previousPrice(component.getPreviousPrice())
                .inStock(component.isInStock())
                .stockUnits(component.getStockUnits())
                .imageUrl(component.getImageUrl())
                .productUrl(component.getProductUrl())
                .attributes(component.getAttributes())
                .tags(component.getTags())
                .build();
    }

    /**
     * Convert ComponentDto to domain Component.
     */
    public Component toDomain(ComponentDto dto) {
        if (dto == null) {
            return null;
        }

        return Component.builder()
                .id(dto.getId())
                .name(dto.getName())
                .brand(dto.getBrand())
                .category(dto.getCategory())
                .price(dto.getPrice())
                .previousPrice(dto.getPreviousPrice())
                .inStock(dto.isInStock())
                .stockUnits(dto.getStockUnits())
                .imageUrl(dto.getImageUrl())
                .productUrl(dto.getProductUrl())
                .attributes(dto.getAttributes())
                .tags(dto.getTags())
                .build();
    }

    /**
     * Convert list of Components to list of ComponentDtos.
     */
    public List<ComponentDto> toDtoList(List<Component> components) {
        if (components == null) {
            return List.of();
        }

        return components.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of ComponentDtos to list of Components.
     */
    public List<Component> toDomainList(List<ComponentDto> dtos) {
        if (dtos == null) {
            return List.of();
        }

        return dtos.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
}
