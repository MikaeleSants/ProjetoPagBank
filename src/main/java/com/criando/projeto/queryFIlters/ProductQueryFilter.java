package com.criando.projeto.queryFIlters;
import com.criando.projeto.entities.Product;
import static com.criando.projeto.specifications.ProductSpec.*;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

@Data

public class ProductQueryFilter {
    private String name;
    private String categoryName;
    private Double minPrice;
    private Double maxPrice;

    public Specification<Product> toSpecification() {
        Specification<Product> spec = Specification.where(null);

        if (name != null && !name.isEmpty()) {
            spec = spec.and(nameContains(name));
        }

        if (categoryName != null && !categoryName.isEmpty()) {
            spec = spec.and(categoryNameContains(categoryName));
        }

        if (minPrice != null) {
            spec = spec.and(priceGreaterThanOrEqualTo(minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and(priceLessThanOrEqualTo(maxPrice));
        }

        return spec;
    }
}
