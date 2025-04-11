package com.criando.projeto.repositories;

import com.criando.projeto.entities.Category;
import com.criando.projeto.entities.Order;
import com.criando.projeto.entities.OrderItem;
import com.criando.projeto.entities.Product;
import com.criando.projeto.entities.enums.OrderStatus;
import com.criando.projeto.entities.User;
import com.criando.projeto.entities.enums.UserRole;
import com.criando.projeto.entities.pk.OrderItemPk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class OrderItemRepositoryTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Order order;
    private Product product;
    private OrderItem orderItem;

    @BeforeEach
    void setup() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Criando usuário
        User user = new User(null, "Mikaele", "mika@test.com", "123456789", "Mik@123", UserRole.ADMIN);
        userRepository.save(user);

        // Criando pedido
        order = new Order(null, Instant.parse("2019-06-20T19:53:07Z"), OrderStatus.WAITING_PAYMENT, user);
        orderRepository.save(order);

        // Criando categoria e produto
        Category category = new Category(null, "Tecnologia");
        categoryRepository.save(category);

        product = new Product(null, "Monitor", "Monitor Full HD", 1200.0);
        product.getCategories().add(category);
        productRepository.save(product);

        // Criando item de pedido
        orderItem = new OrderItem(order, product, 2);
        orderItemRepository.save(orderItem);
    }

    @Test
    @DisplayName("Deve buscar OrderItem por chave composta")
    void deveBuscarOrderItemPorIdComposto() {
        OrderItemPk pk = orderItem.getId();
        Optional<OrderItem> result = orderItemRepository.findById(pk);

        assertThat(result).isPresent();
        assertThat(result.get().getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve salvar um item de pedido")
    void deveSalvarOrderItem() {
        Optional<OrderItem> result = orderItemRepository.findById(orderItem.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve retornar todos os itens de pedido")
    void deveRetornarTodosOrderItems() {
        List<OrderItem> itens = orderItemRepository.findAll();
        assertThat(itens).hasSize(1);
        assertThat(itens.get(0).getProduct().getName()).isEqualTo("Monitor");
    }

    @Test
    @DisplayName("Deve atualizar a quantidade de um OrderItem")
    void deveAtualizarOrderItem() {
        // Recupera o item existente
        Optional<OrderItem> optional = orderItemRepository.findById(orderItem.getId());
        assertThat(optional).isPresent();

        OrderItem item = optional.get();
        item.setQuantity(5); // atualiza a quantidade
        orderItemRepository.save(item); // persiste novamente

        Optional<OrderItem> atualizado = orderItemRepository.findById(orderItem.getId());
        assertThat(atualizado).isPresent();
        assertThat(atualizado.get().getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("Deve deletar item de pedido")
    void deveDeletarOrderItem() {
        orderItemRepository.deleteById(orderItem.getId());
        Optional<OrderItem> result = orderItemRepository.findById(orderItem.getId());
        assertThat(result).isEmpty();
    }
}
