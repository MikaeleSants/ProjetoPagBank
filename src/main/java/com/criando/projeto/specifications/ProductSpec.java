package com.criando.projeto.specifications;

import com.criando.projeto.entities.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

public class ProductSpec {

    //filtro por nome
    public static Specification<Product> nameContains(String name) {
        return (root, query, builder) -> {
            if (ObjectUtils.isEmpty(name)) {
                return null;
            }
            return builder.like(root.get("name"), "%" + name + "%");
        };
    }
    //request: products?name=Mochila


    //filtro por categoria
    public static Specification<Product> categoryNameContains(String categoryName) {
        return (root, query, builder) -> {
            if (ObjectUtils.isEmpty(categoryName)) {
                return null;
            }
            // Como Category é uma coleção, usa um JOIN
            Join<Object, Object> join = root.join("categories", JoinType.INNER);
            return builder.like(join.get("name"), "%" + categoryName + "%");
        };
    }
    //request: http://localhost:8080/products?categoryName=Eletrônicos


    //Filtro por preço
    public static Specification<Product> priceGreaterThanOrEqualTo(Double minPrice) {
        return (root, query, builder) -> {
            if (minPrice == null) {
                return null;
            }
            return builder.greaterThanOrEqualTo(root.get("price"), minPrice);
        };
    }

    public static Specification<Product> priceLessThanOrEqualTo(Double maxPrice) {
        return (root, query, builder) -> {
            if (maxPrice == null) {
                return null;
            }
            return builder.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }
    //requests:
    //1. products?minPrice=100
    //2. products?maxPrice=500
    //3. products?minPrice=100&maxPrice=500

}
