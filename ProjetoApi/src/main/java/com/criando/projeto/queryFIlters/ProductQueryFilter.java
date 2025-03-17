package com.criando.projeto.queryFIlters;
import com.criando.projeto.entities.Product;
import static com.criando.projeto.specifications.ProductSpec.*;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

@Data
//A anotação @Data do Lombok gera automaticamente vários métodos, tipo os get e set
public class ProductQueryFilter {
    private String name;
    private Long categoryId;
    private String categoryName;

    public Specification<Product> toSpecification() {
        Specification<Product> spec = Specification.where(null);

        if (name != null && !name.isEmpty()) {
            spec = spec.and(nameContains(name));
        }

        if (categoryName != null && !categoryName.isEmpty()) {
            spec = spec.and(categoryNameContains(categoryName));
        }

        return spec;
    }
}
