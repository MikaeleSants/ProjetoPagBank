package com.criando.projeto.entities;

import com.criando.projeto.entities.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    @OneToMany(mappedBy = "id.order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> items = new HashSet<>();
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;
    /* CascadeType.ALL significa que qualquer operação feita em Order
     será replicada automaticamente para Payment.
     Se apagar um Order, o Payment correspondente também será apagado.
     Se salvar um Order, o Payment associado será salvo automaticamente. */
    @OneToOne
    @JoinColumn(name = "coupon_id", nullable = true)
    private Coupon discount;


    public Order() {
    }

    public Order(Long id, Instant moment, OrderStatus orderStatus, User client) {
        this.id = id;
        this.moment = moment;
        setOrderStatus(orderStatus);
        this.client = client;
    }

    public Order(Long id, OrderStatus orderStatus, User client, Set<OrderItem> items, Coupon discount, Payment payment) {
        this.id = id;
        setOrderStatus(orderStatus);
        this.client = client;
        this.items = items != null ? items : new HashSet<>();
        this.discount = discount != null ? discount : null;
        this.payment = payment;
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
        return items;
    }
    public void setItems(Set<OrderItem> items) {
        this.items = (items != null) ? items : new HashSet<>();
    }
    //Se items não for null, ele simplesmente atribui o valor recebido à variável this.items.
    //Se items for null, ele cria um novo HashSet<>() vazio, evitando NullPointerException.
    public void addItem(OrderItem item) {
        if (item != null) {
            this.items.add(item);
        }
    }

    public Coupon getDiscount() {
        return discount;
    }
    public void setDiscount(Coupon discount) {
        this.discount = (discount != null) ? discount : null;
    }
    public void applyCoupon(Coupon coupon) {
        this.discount = coupon;
    }

    @PrePersist
    public void prePersist() {
        if (this.moment == null) {
            this.moment = Instant.now(); // Define a data e hora atuais apenas se for nulo
        }
    }

    public Double getTotal() {
        Double total = 0.0;
        for (OrderItem item : items) {
            total += item.getSubTotal();
        }
        // Se houver um cupom de desconto
        if (discount != null) {
            total -= total * (discount.getDiscountPercentage() / 100);
        }

        // Arredondando o total para duas casas decimais
        BigDecimal totalBigDecimal = new BigDecimal(String.valueOf(total)).setScale(2, RoundingMode.DOWN);
        //Usar new BigDecimal(double) pode não ser preciso devido a problemas internos de representação de ponto flutuante (double).
        // O recomendado é sempre converter double para String antes de criar um BigDecimal

        // Retorna o total com duas casas decimais
        return totalBigDecimal.doubleValue();
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
