package com.criando.projeto.entities;

import com.criando.projeto.entities.pk.OrderItemPk;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.springframework.format.annotation.NumberFormat;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "tb_order_item")
public class OrderItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private OrderItemPk id = new OrderItemPk();
    private Integer quantity;
    @NumberFormat(pattern = "#,##0.00")
    private Double price;

    public OrderItem() {
    }
    public OrderItem(Order order, Product product, Integer quantity) {
        id.setOrder(order);
        id.setProduct(product);
        this.quantity = quantity;
        this.price = product.getPrice();
    }

    @JsonIgnore
    public Order getOrder() {
        return id.getOrder();
    }
    public void setOrder(Order order) {
        id.setOrder(order);
    }
    public Product getProduct() {
        return id.getProduct();
    }
    public void setProduct(Product product) {
        id.setProduct(product);
        this.price = product.getPrice();
    }
    public Integer getQuantity() {
        return quantity;
    }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    public Double getPrice() {
        return price;
    }
    public void setPrice(Double price) {
        this.price = price;
    }

    public OrderItemPk getId() {
        return id;
    }

    public Double getSubTotal() {
        return price * quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(id, orderItem.id);
    }
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
