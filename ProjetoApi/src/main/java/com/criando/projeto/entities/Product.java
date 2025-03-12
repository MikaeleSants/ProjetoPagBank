package com.criando.projeto.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "tb_product")
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "O nome do produto não pode estar vazio.")
    private String name;
    @NotBlank
    private String description;
    @NotBlank
    @Positive
    private Double price;
    /*
    * @ManyToMany // Indica um relacionamento muitos-para-muitos entre produtos e categorias
    * @JoinTable(name = "tb_product_category", // Define o nome da tabela intermediária no banco de dados
    * joinColumns = @JoinColumn(name = "product_id"), // Define a chave estrangeira que referencia a tabela de produtos
    * inverseJoinColumns = @JoinColumn(name = "category_id") // Define a chave estrangeira que referencia a tabela de categorias)
    * Isso significa que um Produto pode estar em várias Categorias, e uma Categoria pode conter vários Produtos,
    * sendo gerenciada pela tabela intermediária tb_product_category.*/
    @ManyToMany
    @JoinTable(name = "tb_product_category",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();
    @OneToMany(mappedBy = "id.product")
    private Set<OrderItem> items = new HashSet<>();

    public Product() {
    }
    //n bota a coleção dentro do construtor pq ela já ta sendo instanciada ali em cima
    public Product(Long id, String name, String description, Double price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Double getPrice() {
        return price;
    }
    public void setPrice(Double price) {
        this.price = price;
    }
    public Set<Category> getCategories() {
        return categories;
    }
    @JsonIgnore
    public Set<Order> getOrders() {
        Set<Order> set = new HashSet<>();
        for (OrderItem x : items) {
            set.add(x.getOrder());
        }
        return set;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
