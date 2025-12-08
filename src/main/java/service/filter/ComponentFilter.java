package service.filter;

import com.pcBuilder.backend.model.component.ComponentCategory;
import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class ComponentFilter {

    private ComponentCategory category;
    private Double minPrice;
    private Double maxPrice;
    private String brand;
    private String nameContains;
    private Boolean inStock;
    @Builder.Default
    private Set<String> tags = Collections.emptySet();
    private Sort sort;
    private Integer limit;

    public Set<String> normalizedTags() {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> normalized = new HashSet<>();
        for (String tag : tags) {
            if (tag != null) {
                normalized.add(tag.toLowerCase());
            }
        }
        return normalized;
    }

    public enum Sort {
        PRICE_ASC,
        PRICE_DESC,
        UPDATED_DESC
    }
}






