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

/*
root.get("name") -> Pega o atributo name do Product.
builder.like(...) -> Cria um filtro para encontrar qualquer produto que contenha essa string no nome.
"%" + name + "%" -> O % significa "qualquer coisa antes ou depois", permitindo buscas parciais.
 */

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

     /*
1. root.join("categories", JoinType.INNER)

- O root representa a tabela principal da consulta, que é Product.
- O metodo .join("categories", JoinType.INNER) conecta a tabela Product com a tabela Category, porque um produto pode ter várias categorias (@ManyToMany).
- "categories" é o nome do atributo da entidade Product que armazena a relação com Category.
2. Join<Object, Object>
- O Join<,> representa a conexão entre duas tabelas no banco.
- O primeiro Object representa a entidade de origem (Product).
- O segundo Object representa a entidade de destino (Category).

Lendo a linha como uma frase
"Pegue a tabela Product e faça um JOIN com a tabela Category, conectando os produtos às suas categorias".
Depois desse JOIN, podemos acessar os atributos da Category (como name) para aplicar filtros.
     */

    //Filtro por preço
    public static Specification<Product> priceGreaterThanOrEqualTo(Double minPrice) {
        return (root, query, builder) -> {
            if (minPrice == null) {
                return null;
            }
            return builder.greaterThanOrEqualTo(root.get("price"), minPrice);
        };
    }
    //requests:
    // 1. products?minPrice=100
    public static Specification<Product> priceLessThanOrEqualTo(Double maxPrice) {
        return (root, query, builder) -> {
            if (maxPrice == null) {
                return null;
            }
            return builder.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }
    //2. products?maxPrice=500

    //3. products?minPrice=100&maxPrice=500

}
