package com.criando.projeto.specifcations;

import com.criando.projeto.entities.Product;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

public class ProductSpec {
    public static Specification<Product> nameContains(String name) {
        return (root, query, builder) -> {
            if (ObjectUtils.isEmpty(name)) {
                return null;
            }
            return builder.like(root.get("name"), "%" + name + "%");
        };
    }
}
