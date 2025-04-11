package com.criando.projeto.repositories;

import com.criando.projeto.entities.Order;
import com.criando.projeto.entities.User;
import com.criando.projeto.entities.enums.OrderStatus;
import com.criando.projeto.entities.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.time.Instant;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private User client;
    private Order order;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        userRepository.deleteAll();

        client = new User(null, "Maria", "maria@example.com", "123456789","Mar@123", UserRole.ADMIN);
        userRepository.save(client);

        order = new Order(null, Instant.parse("2019-06-20T19:53:07Z"), OrderStatus.WAITING_PAYMENT, client);
        order = orderRepository.save(order);

        Order segundaOrder = new Order(null, Instant.parse("2019-06-20T19:53:07Z"), OrderStatus.WAITING_PAYMENT, client);
        orderRepository.save(segundaOrder);
    }

    @Test
    @DisplayName("Deve retornar todas as orders")
    void deveRetornarTodasOrders() {
        var todasOrders = orderRepository.findAll();

        assertThat(todasOrders).hasSize(2);
        assertThat(todasOrders)
                .extracting(Order::getOrderStatus)
                .contains(OrderStatus.WAITING_PAYMENT);
    }

    @Test
    @DisplayName("Deve encontrar order por ID")
    void deveEncontrarOrderPorId() {
        Optional<Order> found = orderRepository.findById(order.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getClient().getEmail()).isEqualTo("maria@example.com");
    }

    @Test
    @DisplayName("Deve verificar se existe order por ID e email do cliente")
    void deveVerificarSeExisteOrderPorIdEEmail() {
        boolean exists = orderRepository.existsByIdAndClient_Email(order.getId(), "maria@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false se email do cliente não bater com o ID da order")
    void deveRetornarFalseSeEmailIncorreto() {
        boolean exists = orderRepository.existsByIdAndClient_Email(order.getId(), "outra@email.com");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve salvar uma nova order")
    void deveSalvarOrder() {
        Order novaOrder = new Order(null, Instant.parse("2019-06-20T19:53:07Z"), OrderStatus.PAID, client);
        Order salva = orderRepository.save(novaOrder);

        assertThat(salva.getId()).isNotNull();
        assertThat(salva.getOrderStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(salva.getClient().getEmail()).isEqualTo("maria@example.com");
    }

    @Test
    @DisplayName("Deve atualizar status da order")
    void deveAtualizarOrder() {
        order.setOrderStatus(OrderStatus.PAID);
        Order atualizada = orderRepository.save(order);

        assertThat(atualizada.getOrderStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("Deve deletar uma order pelo ID")
    void deveDeletarOrder() {
        orderRepository.deleteById(order.getId());
        Optional<Order> resultado = orderRepository.findById(order.getId());

        assertThat(resultado).isEmpty();
    }
}