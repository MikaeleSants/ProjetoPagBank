package com.criando.projeto.resource;

import com.criando.projeto.entities.*;
import com.criando.projeto.entities.enums.OrderStatus;
import com.criando.projeto.entities.enums.PaymentMethod;
import com.criando.projeto.entities.enums.UserRole;
import com.criando.projeto.queryFIlters.OrderQueryFilter;
import com.criando.projeto.repositories.OrderRepository;
import com.criando.projeto.services.OrderServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import java.net.URI;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderResourcesTest {
    @InjectMocks
    private OrderResources orderResources;
    @Mock
    private OrderServices orderServices;
    @Mock
    private Authentication authentication;
    @Mock
    private UserDetails userDetails;

    private Order order;
    private Order orderDois;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Fulano", "fulano@email.com", "11999999999", "Sen@123", UserRole.USER);
        order = new Order(1L, Instant.now(), OrderStatus.WAITING_PAYMENT, user);
        orderDois = new Order(2L, Instant.now(), OrderStatus.PAID, user);
    }

    private OrderItem createOrderItem() {
        Product product = new Product(2L, "Produto Teste", "Descrição", 50.0);
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(3);
        item.setPrice(product.getPrice());
        return item;
    }

    @Test
    @DisplayName("Deve retornar lista de pedidos com status 200 OK")
    void findOrders() {
        List<Order> orders = List.of(order, orderDois);
        OrderQueryFilter filter = new OrderQueryFilter();

        when(orderServices.findOrders(filter)).thenReturn(orders);

        ResponseEntity<List<Order>> result = orderResources.findOrders(filter);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(orders, result.getBody());
        verify(orderServices).findOrders(filter);
    }

    //tá faltando o 401

    @Test
    @DisplayName("Deve retornar o pedido pelo ID com status 200 OK")
    void getOrderById() {
        Long orderId = 1L;

        when(orderServices.findById(orderId, authentication)).thenReturn(order);

        ResponseEntity<Order> result = orderResources.getOrderById(orderId, authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(order, result.getBody());

        verify(orderServices).findById(orderId, authentication);
    }

    @Test
    @DisplayName("Deve criar um pedido e retornar status 201 com URI")
    void insert_DeveCriarPedidoERetornar201() {
        Order pedidoParaInserir = new Order(null, Instant.now(), OrderStatus.WAITING_PAYMENT, user);
        Order pedidoSalvo = new Order(1L, pedidoParaInserir.getMoment(), pedidoParaInserir.getOrderStatus(), user);

        URI fakeUri = URI.create("/orders/1");
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = Mockito.mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = Mockito.mock(ServletUriComponentsBuilder.class);
            UriComponents uriComponents = Mockito.mock(UriComponents.class);

            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentRequest).thenReturn(builder);
            when(builder.path("/{id}")).thenReturn(builder);
            when(builder.buildAndExpand(pedidoSalvo.getId())).thenReturn(uriComponents);
            when(uriComponents.toUri()).thenReturn(fakeUri);

            when(orderServices.insert(any(Order.class))).thenReturn(pedidoSalvo);

            ResponseEntity<Order> response = orderResources.insert(pedidoParaInserir);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(pedidoSalvo, response.getBody());
            assertEquals(fakeUri, response.getHeaders().getLocation());

            verify(orderServices).insert(any(Order.class));
        }
    }

    @Test
    @DisplayName("Deve aplicar cupom ao pedido e retornar status 200 OK")
    void applyCouponToOrder_DeveRetornarPedidoAtualizado() {
        Long couponId = 2L;
        Order pedidoAtualizado = new Order();
        pedidoAtualizado.setId(order.getId());
        when(orderServices.setOrDeleteCoupon(order.getId(), couponId)).thenReturn(pedidoAtualizado);
        ResponseEntity<Order> response = orderResources.applyCouponToOrder(order.getId(), couponId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pedidoAtualizado, response.getBody());
        verify(orderServices).setOrDeleteCoupon(order.getId(), couponId);
    }

    @Test
    @DisplayName("Deve atualizar pagamento do pedido e retornar status 200 OK")
    void updateOrderPayment_DeveAtualizarEretornarPedido() {
        Payment pagamento = new Payment(order.getId(), Instant.now(), null, PaymentMethod.CREDIT_CARD);
        Order pedidoAtualizado = new Order();
        pedidoAtualizado.setId(order.getId());
        pedidoAtualizado.setPayment(pagamento);
        when(orderServices.setOrderPayment(order.getId(), pagamento)).thenReturn(pedidoAtualizado);
        ResponseEntity<Order> response = orderResources.updateOrderPayment(order.getId(), pagamento);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pedidoAtualizado, response.getBody());
        verify(orderServices).setOrderPayment(order.getId(), pagamento);
    }

    @Test
    @DisplayName("Deve atualizar um pedido existente e retornar status 200 OK")
    void updateOrder() {
        // Arrange
        Long orderId = 1L;

        Order orderParaAtualizar = new Order();
        orderParaAtualizar.setMoment(Instant.now());
        orderParaAtualizar.setOrderStatus(OrderStatus.WAITING_PAYMENT);
        orderParaAtualizar.setClient(user);

        Order orderAtualizado = new Order(orderId, orderParaAtualizar.getMoment(), OrderStatus.PAID, user);

        when(orderServices.update(eq(orderId), any(Order.class))).thenReturn(orderAtualizado);

        // Act
        ResponseEntity<Order> response = orderResources.update(orderId, orderParaAtualizar);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(orderAtualizado, response.getBody());
        assertEquals(OrderStatus.PAID, response.getBody().getOrderStatus());

        verify(orderServices).update(eq(orderId), any(Order.class));
    }

    @Test
    @DisplayName("Deve deletar um pedido existente e retornar status 204 No Content")
    void deleteOrder() {
        // Arrange
        Long orderId = 1L;
        doNothing().when(orderServices).delete(orderId);

        // Act
        ResponseEntity<Void> response = orderResources.delete(orderId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(orderServices).delete(orderId);
    }

    @Test
    @DisplayName("Deve remover um produto do pedido e retornar o pedido atualizado com status 200 OK")
    void removeProductFromOrder_sucesso() {
        // Arrange
        Long orderId = 1L;
        Long productId = 2L;
        Order pedidoAtualizado = new Order(orderId, Instant.now(), OrderStatus.WAITING_PAYMENT, user);

        when(orderServices.removeProductFromOrder(orderId, productId)).thenReturn(pedidoAtualizado);

        // Act
        ResponseEntity<Order> response = orderResources.removeProductFromOrder(orderId, productId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pedidoAtualizado, response.getBody());
        verify(orderServices).removeProductFromOrder(orderId, productId);
    }

    @Test
    @DisplayName("Deve atualizar o status do pedido e retornar o pedido atualizado com status 200 OK")
    void updateOrderStatus_deveAtualizarStatusComSucesso() {
        // Arrange
        Long orderId = 1L;
        String novoStatus = "PAID";
        Map<String, String> statusUpdate = new HashMap<>();
        statusUpdate.put("orderStatus", novoStatus);

        Order pedidoAtualizado = new Order(orderId, Instant.now(), OrderStatus.valueOf(novoStatus), user);

        when(orderServices.updateOrderStatus(orderId, OrderStatus.valueOf(novoStatus)))
                .thenReturn(pedidoAtualizado);

        // Act
        ResponseEntity<Order> response = orderResources.updateOrderStatus(orderId, statusUpdate);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(OrderStatus.PAID, response.getBody().getOrderStatus());
        assertEquals(pedidoAtualizado, response.getBody());
        verify(orderServices).updateOrderStatus(orderId, OrderStatus.PAID);
    }

    @Test
    @DisplayName("Deve permitir usuário atualizar os itens do pedido")
    void updateOrderItems() {
        Long orderId = 1L;

        // Itens para atualizar no pedido
        OrderItem orderItem = createOrderItem();
        Set<OrderItem> novosItens = Set.of(orderItem);

        // Pedido atualizado com os novos itens
        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setItems(novosItens);

        // Mock de autenticação
        Authentication authentication = mock(Authentication.class);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        doReturn(authorities).when(authentication).getAuthorities();
        doReturn(authentication).when(authentication).getPrincipal();
        doReturn(user.getEmail()).when(authentication).getName();

        // Configuração do SecurityContext
        SecurityContext securityContext = mock(SecurityContext.class);
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        // Mock do serviço
        doReturn(updatedOrder)
            .when(orderServices)
            .updateOrderItems(orderId, novosItens, authentication);

        // Act
        ResponseEntity<Order> response = orderResources.updateOrderItems(orderId, novosItens);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedOrder, response.getBody());
        verify(orderServices).updateOrderItems(orderId, novosItens, authentication);
    }
}