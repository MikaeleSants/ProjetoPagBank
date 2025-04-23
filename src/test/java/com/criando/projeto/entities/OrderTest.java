package com.criando.projeto.entities;

import com.criando.projeto.entities.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Product product;
    private OrderItem item;
    private Coupon coupon;
    private Order order;

    @BeforeEach
    void setUp() {
        product = new Product(1L, "Notebook", "Dell XPS", 3000.00);
        item = new OrderItem();
        item.setProduct(product);
        item.setPrice(product.getPrice());
        item.setQuantity(2);

        coupon = new Coupon(1L, "DESC10", 10.0);

        User user = new User(1L, "Maria", "maria@email.com", "999999999", "senha", null);
        order = new Order(1L, Instant.now(), OrderStatus.WAITING_PAYMENT, user);
        order.addItem(item);
    }

    @Test
    @DisplayName("Deve calcular o total corretamente sem cupom")
    void testGetTotalSemDesconto() {
        double totalEsperado = 2 * product.getPrice();
        assertEquals(totalEsperado, order.getTotal());
    }

    @Test
    @DisplayName("Deve aplicar desconto corretamente com cupom")
    void testGetTotalComDesconto() {
        order.setDiscount(coupon);
        double totalEsperado = 2 * product.getPrice() * 0.9;
        BigDecimal esperadoArredondado = new BigDecimal(totalEsperado).setScale(2, RoundingMode.DOWN);

        assertEquals(esperadoArredondado.doubleValue(), order.getTotal());
    }

    @Test
    @DisplayName("Deve setar data automaticamente se nula no persist")
    void testPrePersist() {
        order.setMoment(null);
        order.prePersist();
        assertNotNull(order.getMoment());
    }
}
