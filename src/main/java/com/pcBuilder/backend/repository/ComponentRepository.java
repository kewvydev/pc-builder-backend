package com.pcBuilder.backend.repository;

import com.pcBuilder.backend.model.component.ComponentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ComponentEntity database operations.
 */
@Repository
public interface ComponentRepository extends JpaRepository<ComponentEntity, String>, 
                                             JpaSpecificationExecutor<ComponentEntity> {

    /**
     * Find all components by category.
     */
    List<ComponentEntity> findByCategory(String category);

    /**
     * Find all components by brand.
     */
    List<ComponentEntity> findByBrand(String brand);

    /**
     * Find components by category and in stock status.
     */
    List<ComponentEntity> findByCategoryAndInStock(String category, Boolean inStock);

    /**
     * Find components with price between min and max.
     */
    List<ComponentEntity> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Find components by category and price range.
     */
    List<ComponentEntity> findByCategoryAndPriceBetween(String category, BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Search components by name containing text (case insensitive).
     */
    @Query("SELECT c FROM ComponentEntity c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<ComponentEntity> searchByName(@Param("searchText") String searchText);

    /**
     * Search components by name and category.
     */
    @Query("SELECT c FROM ComponentEntity c WHERE c.category = :category AND LOWER(c.name) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<ComponentEntity> searchByNameAndCategory(@Param("searchText") String searchText, @Param("category") String category);

    /**
     * Find components by tag.
     */
    @Query("SELECT DISTINCT c FROM ComponentEntity c JOIN c.tags t WHERE LOWER(t.tag) = LOWER(:tag)")
    List<ComponentEntity> findByTag(@Param("tag") String tag);

    /**
     * Find components that are in stock.
     */
    List<ComponentEntity> findByInStockTrue();

    /**
     * Count components by category.
     */
    long countByCategory(String category);

    /**
     * Check if a component exists by ID.
     */
    boolean existsById(String id);
}

