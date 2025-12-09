package com.pcBuilder.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Lightweight DTO for component listings.
 * Does NOT include attributes or tags to avoid heavy data transfer.
 * Use ComponentDto when you need the full component details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentListDto {
    
    private String id;
    private String category;
    private String name;
    private String brand;
    private BigDecimal price;
    private BigDecimal previousPrice;
    private String imageUrl;
    private String productUrl;
    private boolean inStock;
    private int stockUnits;
    
    /**
     * Constructor for JPA projection queries.
     */
    public ComponentListDto(String id, String category, String name, String brand, 
                           BigDecimal price, BigDecimal previousPrice, 
                           String imageUrl, String productUrl, 
                           Boolean inStock, Integer stockUnits) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.previousPrice = previousPrice;
        this.imageUrl = imageUrl;
        this.productUrl = productUrl;
        this.inStock = inStock != null ? inStock : true;
        this.stockUnits = stockUnits != null ? stockUnits : 0;
    }
}

