package com.estudando.curso.entities;

import com.estudando.curso.entities.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "tb_order")
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "GMT")
    private Instant moment;
    private Integer orderStatus;
    /* a relação entre order e user é de manytoone, o que signifca que
    * serão varios pedidos por pessoas, para indicar esse tipo de relação como chave estrageira
    * para o JPA, se usa a annotation @ManytoOne e o @JoinColumn para adicionar
    * mais uma coluna com essa associação na tabela e dar um nome pra ela*/
    @ManyToOne
    @JoinColumn(name = "client_id")
    private User client;
    @OneToMany(mappedBy = "id.order")
    private Set<OrderItem> Items = new HashSet<>();
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    /* CascadeType.ALL significa que qualquer operação feita em Order
     será replicada automaticamente para Payment.
     Se apagar um Order, o Payment correspondente também será apagado.
     Se salvar um Order, o Payment associado será salvo automaticamente. */
    private Payment payment;


    public Order() {
    }
    public Order(Long id, Instant moment, OrderStatus orderStatus, User client) {
        this.id = id;
        this.moment = moment;
        setOrderStatus(orderStatus);
        this.client = client;
    }



    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Instant getMoment() {
        return moment;
    }
    public void setMoment(Instant moment) {
        this.moment = moment;
    }
    public OrderStatus getOrderStatus() {
        return OrderStatus.valueOf(orderStatus); //convertendo de intenger p OrderStatus
    }
    public void setOrderStatus(OrderStatus orderStatus) {
        if (orderStatus != null) {
        this.orderStatus = orderStatus.getCode();}
    }
    public User getClient() {
        return client;
    }
    public void setClient(User client) {
        this.client = client;
    }
    public Payment getPayment() {
        return payment;
    }
    public void setPayment(Payment payment) {
        this.payment = payment;
    }
    public Set<OrderItem> getItems() {
        return Items;
    }
    public Double getTotal() {
        Double total = 0.0;
        for (OrderItem item : Items) {
            total += item.getSubTotal();
        } return total;
    }



    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
