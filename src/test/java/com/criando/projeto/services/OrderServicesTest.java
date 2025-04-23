package com.criando.projeto.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import com.criando.projeto.entities.*;
import com.criando.projeto.entities.enums.PaymentMethod;
import com.criando.projeto.entities.enums.UserRole;
import com.criando.projeto.queryFIlters.OrderQueryFilter;
import com.criando.projeto.repositories.*;
import com.criando.projeto.services.exceptions.*;
import org.aspectj.weaver.ast.Or;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import com.criando.projeto.entities.enums.OrderStatus;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class OrderServicesTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CouponRepository couponRepository;
    @Mock
    private AuthenticationFacade authenticationFacade;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderServices orderService;

    private Order order;
    private Order orderDois;
    private User user;
    private User admin;
    private Product product;
    private OrderItem orderItem;
    private OrderItem orderItemDois;
    private Coupon coupon;
    private User currentUser;
    private Order updateOrder;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        user = new User(1L, "Fulano de Tal", "fulano@example.com", "11999998888", "Sen@123", UserRole.USER);
        admin = new User(2L, "Fulano Admin", "admin@example.com", "11999997777", "Adm@123", UserRole.ADMIN);

        // Definindo o usuário que será autenticado
        currentUser = user;  // Por padrão, autenticado como user

        // Mock de autenticação com base no usuário configurado
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getName()).thenReturn(currentUser.getEmail());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Configuração do authenticationFacade
        lenient().when(authenticationFacade.getAuthenticatedUserEmail()).thenReturn(currentUser.getEmail());
        lenient().when(authenticationFacade.isUser(authentication)).thenReturn(currentUser.getRole() == UserRole.USER);
        lenient().when(authenticationFacade.getAuthenticatedUser()).thenReturn(currentUser);
        lenient().when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        lenient().when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        lenient().when(authenticationFacade.isSameUser(anyLong())).thenAnswer(invocation -> {
            Long userId = invocation.getArgument(0);
            return userId.equals(currentUser.getId());
        });

        product = new Product(1L, "Smartphone XYZ", "Modelo 2023 com 128GB", 2500.00);

        order = new Order(1L, Instant.parse("2019-06-20T19:53:07Z"), OrderStatus.WAITING_PAYMENT, user);
        orderItem = new OrderItem(order, product, 1);

        orderDois = new Order(2L, Instant.parse("2019-06-20T19:53:07Z"), OrderStatus.WAITING_PAYMENT, admin);
        orderItemDois = new OrderItem(orderDois, product, 1);

        Coupon coupon = createCoupon();
        lenient().when(couponRepository.findById(coupon.getId())).thenReturn(Optional.of(coupon));
    }

    // Metodo para configurar o teste como admin
    void setUpAsAdmin() {
        currentUser = admin;
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getName()).thenReturn(currentUser.getEmail());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Configura o authenticationFacade para admin
        lenient().when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        lenient().when(authenticationFacade.getAuthenticatedUserEmail()).thenReturn(currentUser.getEmail());
        lenient().when(authenticationFacade.isUser(authentication)).thenReturn(false);
        lenient().when(authenticationFacade.isAdmin(any())).thenReturn(true);
        lenient().when(authenticationFacade.getAuthenticatedUser()).thenReturn(currentUser);
        lenient().when(authenticationFacade.isSameUser(anyLong())).thenAnswer(invocation -> {
            Long adminId = invocation.getArgument(0);
            return adminId.equals(currentUser.getId());
        });
        lenient().when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
    }

    private Coupon createCoupon() {
        return new Coupon(1L, "DESCONTO10", 10.0);
    }

    private Payment createPayment(Order order) {
        return new Payment(1L, Instant.now(), order, PaymentMethod.CREDIT_CARD);
    }

    private Order createUpdateOrder(User userPedido, OrderStatus status) {
        Coupon coupon = createCoupon();
        Order order = new Order();
        order.setMoment(Instant.now());
        order.setClient(userPedido);
        order.setOrderStatus(status);
        order.setDiscount(coupon);
        return order;
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
    @DisplayName("Deve retornar todos pedidos quando usuário for ADMIN")
    void findOrders_Admin() {
        // Configuração do mock de autenticação
        setUpAsAdmin(); // Configura a autenticação para o admin
        when(orderRepository.findAll(any(Specification.class))).thenReturn(List.of(order, orderDois));
        List<Order> result = orderService.findOrders(new OrderQueryFilter());
        assertEquals(2, result.size());
        verify(orderRepository).findAll(any(Specification.class));
        verify(authenticationFacade).isUser(any(Authentication.class));
        verify(authenticationFacade, never()).isAdmin(any(Authentication.class));  // Agora verificando o comportamento de admin
    }

    @Test
    @DisplayName("Deve retornar apenas pedidos do usuário autenticado quando role for USER")
    void findOrders_User() {
        OrderQueryFilter filter = new OrderQueryFilter();
        when(orderRepository.findAll(any(Specification.class))).thenReturn(List.of(order));
        List<Order> result = orderService.findOrders(filter);
        assertEquals(1, result.size());
        assertEquals(user.getId(), result.get(0).getClient().getId());
        verify(authenticationFacade).getAuthenticatedUser();
        verify(orderRepository).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Deve retornar o pedido quando o usuário autenticado for admin")
    void findById_Admin() {
        setUpAsAdmin();
        when(orderRepository.findById(2L)).thenReturn(Optional.of(orderDois));
        Order resultado = orderService.findById(2L, SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(resultado);
        assertEquals(2L, resultado.getId());
        assertEquals(admin.getId(), resultado.getClient().getId()); // orderDois pertence ao admin
        verify(orderRepository).findById(2L);
    }

    @Test
    @DisplayName("Deve retornar o pedido quando o usuário autenticado for o dono do pedido")
    void findById_User() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        Order resultado = orderService.findById(1L, SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(user.getId(), resultado.getClient().getId());
    }

    @Test
    @DisplayName("Deve lançar AccessDeniedException quando o usuário não for admin nem o dono do pedido")
    void findById_AccessDeniedException() {
        when(orderRepository.findById(2L)).thenReturn(Optional.of(orderDois));
        assertThrows(AccessDeniedException.class, () -> {
            orderService.findById(2L, SecurityContextHolder.getContext().getAuthentication());
        });
        verify(authenticationFacade).isSameUser(2L);
        verify(orderRepository).findById(2L);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o pedido não for encontrado")
    void findById_ResourceNotFoundException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.findById(99L, SecurityContextHolder.getContext().getAuthentication());
        });
        verify(orderRepository).findById(99L);
    }

    // estudar a partir daqui
    @Test
    @DisplayName("Deve inserir um pedido corretamente quando dados estão válidos")
    void insert_User() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        Order newOrder = new Order();
        newOrder.setItems(new HashSet<>(Set.of(orderItem)));

        Order savedOrder = new Order(10L, OrderStatus.WAITING_PAYMENT, user, new HashSet<>(), null, null);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);


        OrderItem itemSalvo = new OrderItem();
        itemSalvo.setOrder(savedOrder);
        itemSalvo.setProduct(product);
        itemSalvo.setQuantity(orderItem.getQuantity());
        itemSalvo.setPrice(product.getPrice());
        when(orderItemRepository.saveAll(anySet())).thenReturn(List.of(itemSalvo));

        // Chamando o método de inserção
        Order resultado = orderService.insert(newOrder);


        assertNotNull(resultado);
        assertEquals(savedOrder.getId(), resultado.getId());
        assertEquals(user, resultado.getClient());
        assertEquals(1, resultado.getItems().size());

        OrderItem itemResultado = resultado.getItems().iterator().next();
        assertEquals(product.getId(), itemResultado.getProduct().getId());
        assertEquals(product.getPrice(), itemResultado.getPrice());
        assertEquals(savedOrder, itemResultado.getOrder());


        verify(userRepository).findByEmail(user.getEmail());
        verify(productRepository).findById(product.getId());
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).saveAll(anySet());
    }

    @Test
    @DisplayName("Deve inserir um pedido corretamente quando dados estão válidos, com o user ADMIN")
    void insert_Admin() {
        setUpAsAdmin();
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        Order newOrder = new Order();
        newOrder.setItems(new HashSet<>(Set.of(orderItem)));

        Order savedOrder = new Order(10L, OrderStatus.WAITING_PAYMENT, admin, new HashSet<>(), null, null);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);


        OrderItem itemSalvo = new OrderItem();
        itemSalvo.setOrder(savedOrder);
        itemSalvo.setProduct(product);
        itemSalvo.setQuantity(orderItem.getQuantity());
        itemSalvo.setPrice(product.getPrice());
        when(orderItemRepository.saveAll(anySet())).thenReturn(List.of(itemSalvo));

        // Chamando o método de inserção
        Order resultado = orderService.insert(newOrder);


        assertNotNull(resultado);
        assertEquals(savedOrder.getId(), resultado.getId());
        assertEquals(admin, resultado.getClient());
        assertEquals(1, resultado.getItems().size());

        OrderItem itemResultado = resultado.getItems().iterator().next();
        assertEquals(product.getId(), itemResultado.getProduct().getId());
        assertEquals(product.getPrice(), itemResultado.getPrice());
        assertEquals(savedOrder, itemResultado.getOrder());


        verify(userRepository).findByEmail(admin.getEmail());
        verify(productRepository).findById(product.getId());
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).saveAll(anySet());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando produto não for encontrado")
    void insert_ProductNotFound() {
        Product invalidProduct = new Product();
        product.setId(99L); // ID que não existe

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(invalidProduct);
        orderItem.setQuantity(1);
        Order order = new Order();
        order.setItems(Set.of(orderItem));

        when(productRepository.findById(invalidProduct.getId())).thenReturn(Optional.empty());

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setClient(user);
        savedOrder.setItems(new HashSet<>());
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.insert(order));

        assertEquals("Produto não encontrado: ID " + invalidProduct.getId(), exception.getMessage());

        verify(userRepository).findByEmail(user.getEmail());
        verify(productRepository).findById(invalidProduct.getId());
        verify(orderRepository).save(any(Order.class));
        verifyNoInteractions(orderItemRepository);
    }

    @Test
    @DisplayName("Deve lidar corretamente quando coupon for inválido")
    void insert_InvalidCoupon() {
        // Coupon n persistido
        Coupon nonExistentCoupon = new Coupon();
        nonExistentCoupon.setId(999L); // ID que não existe no banco
        nonExistentCoupon.setCode("INVALID_123");

        Order order = new Order();
        order.setDiscount(nonExistentCoupon);

        when(couponRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {orderService.insert(order);});
        verify(couponRepository).findById(999L);
        verify(userRepository).findByEmail(user.getEmail());
    }

    //ver se valida mesmo a mudança de status
    @Test
    @DisplayName("Deve atualizar o pagamento e o status do pedido para PAID quando dados estão válidos e usuário é admin")
    void setOrderPayment_Admin() {
        setUpAsAdmin();
        Payment payment = createPayment(orderDois);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(orderDois));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.setOrderPayment(1L, payment);

        assertNotNull(result);
        assertEquals(OrderStatus.PAID, result.getOrderStatus());
        assertNotNull(result.getPayment());
        assertEquals(payment.getPaymentMethod(), result.getPayment().getPaymentMethod());
        assertEquals(orderDois, result.getPayment().getOrder());
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve definir pagamento corretamente quando usuário é o dono do pedido")
    void setOrderPayment_UserOwner() {
        Payment payment = createPayment(order);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.setOrderPayment(1L, payment);

        assertNotNull(result);
        assertEquals(OrderStatus.PAID, result.getOrderStatus());
        assertNotNull(result.getPayment());
        assertEquals(payment.getPaymentMethod(), result.getPayment().getPaymentMethod());
        assertEquals(order, result.getPayment().getOrder());
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o pedido não for encontrado")
    /*
     Esse teste não tem de autenticação explícita, porque testa apenas o comportamento
     interno do service. No metodo original eu faço a busca do pedido antes de fazer a autenticação
     (deixando essa parte na responsabilidade dos filtros do springSecurity), porque eu preciso buscar
     o pedido, pra checar o ownership e liberar o acesso pro user. Pode ser um ponto de melhoria.
     */
    void setOrderPayment_OrderNotFound() {
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        Payment payment = new Payment();
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.setOrderPayment(orderId, payment));
        assertEquals("Pedido não encontrado. ID:" + orderId, exception.getMessage());
        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("Deve lançar AccessDeniedException quando o usuário não for admin nem dono do pedido")
    void setOrderPayment_UnauthorizedUser() {
        Long orderId = order.getId();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(authenticationFacade.isAdmin(any())).thenReturn(false);
        when(authenticationFacade.isSameUser(user.getId())).thenReturn(false);
        Payment payment = new Payment();
        assertThrows(AccessDeniedException.class,
                () -> orderService.setOrderPayment(orderId, payment));
        verify(orderRepository).findById(orderId);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"PAID", "CANCELED"})
    @DisplayName("Deve lançar exceção ao tentar alterar pagamento de pedido já finalizado")
    void setOrderPayment_AlreadyPaidOrCanceled(OrderStatus status) {
        Order finalizedOrder = new Order();
        finalizedOrder.setId(2L);
        finalizedOrder.setOrderStatus(status);
        when(orderRepository.findById(any())).thenReturn(Optional.of(finalizedOrder));
        assertThrows(OrderStatusConflictException.class,
                () -> orderService.setOrderPayment(finalizedOrder.getId(), new Payment()));
        verify(orderRepository).findById(2L);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderItemRepository, productRepository);
    }

    @Test
    @DisplayName("Deve aplicar o cupom corretamente quando o usuário é admin")
    void setCoupon_Admin() {
        setUpAsAdmin();

        Coupon coupon = createCoupon();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.setOrDeleteCoupon(1L, 1L);

        assertNotNull(result);
        assertNotNull(result.getDiscount());
        assertEquals(coupon, result.getDiscount());
        verify(orderRepository).findById(1L);
        verify(couponRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve aplicar o cupom corretamente quando o usuário é o dono do pedido")
    void setCoupon_UserOwner() {
        Coupon coupon = createCoupon();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(couponRepository.findById(coupon.getId())).thenReturn(Optional.of(coupon));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.setOrDeleteCoupon(order.getId(), coupon.getId());

        assertNotNull(result);
        assertNotNull(result.getDiscount());
        assertEquals(coupon, result.getDiscount());
        verify(orderRepository).findById(order.getId());
        verify(couponRepository).findById(coupon.getId());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o pedido não for encontrado")
    void setCoupon_OrderNotFound() {
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.setOrDeleteCoupon(orderId, 1L));
        assertEquals("Pedido não encontrado. ID:" + orderId, exception.getMessage());
        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("Deve lançar AccessDeniedException quando o usuário não for admin nem dono do pedido")
    void setCoupon_UnauthorizedUser() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(authenticationFacade.isAdmin(any())).thenReturn(false);
        when(authenticationFacade.isSameUser(any())).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> orderService.setOrDeleteCoupon(1L, 1L));

        verify(orderRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o cupom não for encontrado")
    void setCoupon_CouponNotFound() {
        Long couponId = 999L;
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(couponRepository.findById(couponId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.setOrDeleteCoupon(1L, couponId));
        assertEquals("Cupom não encontrado: ID " + couponId, exception.getMessage());
        verify(orderRepository).findById(1L);
        verify(couponRepository).findById(couponId);
    }

    @Test
    @DisplayName("Deve lançar CouponAlreadyAppliedException quando o cupom já estiver aplicado no pedido")
    void setCoupon_CouponAlreadyApplied() {
        Coupon coupon = createCoupon();
        order.setDiscount(coupon);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(couponRepository.findById(coupon.getId())).thenReturn(Optional.of(coupon));

        CouponAlreadyAppliedException exception = assertThrows(
                CouponAlreadyAppliedException.class,
                () -> orderService.setOrDeleteCoupon(order.getId(), coupon.getId())
        );
        assertEquals("Coupon with ID " + coupon.getId() + " is already applied to the order.",
                exception.getMessage());
        verify(orderRepository).findById(1L);
        verify(couponRepository).findById(1L);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"PAID", "CANCELED"})
    @DisplayName("Deve lançar exceção ao tentar aplicar cupom de pedido finalizado")
    void setCoupon_AlreadyFinalized(OrderStatus status) {
        order.setOrderStatus(status);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(OrderStatusConflictException.class,
                () -> orderService.setOrDeleteCoupon(order.getId(), 1L));

        verify(orderRepository).findById(1L);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(couponRepository);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"PAID", "CANCELED"})
    @DisplayName("Deve lançar exceção ao tentar remover cupom de pedido finalizado")
    void deleteCoupon_AlreadyFinalized(OrderStatus status) {
        order.setOrderStatus(status);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(OrderStatusConflictException.class,
                () -> orderService.setOrDeleteCoupon(order.getId(), null));

        verify(orderRepository).findById(1L);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(couponRepository);
    }

    @Test
    @DisplayName("Deve remover o cupom corretamente quando o usuário é o dono do pedido")
    void deleteCoupon_DeleteCoupon_UserOwner() {
        Coupon coupon = createCoupon();
        order.setDiscount(coupon);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.setOrDeleteCoupon(order.getId(), null);

        assertNotNull(result);
        assertNull(result.getDiscount()); // O cupom deve ser removido
        verify(orderRepository).findById(order.getId());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o pedido não for encontrado ao tentar deletar o cupom")
    void deleteCoupon_OrderNotFound_DeleteCoupon() {
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.setOrDeleteCoupon(orderId, null)); // Tentando deletar o cupom
        assertEquals("Pedido não encontrado. ID:" + orderId, exception.getMessage());
        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("Deve lançar AccessDeniedException quando o usuário não for admin nem dono do pedido ao tentar deletar o cupom")
    void deleteCoupon_UnauthorizedUser_DeleteCoupon() {
        when(orderRepository.findById(orderDois.getId())).thenReturn(Optional.of(orderDois));
        assertThrows(AccessDeniedException.class,
                () -> orderService.setOrDeleteCoupon(orderDois.getId(), null)); // Tentando deletar o cupom
        verify(orderRepository).findById(orderDois.getId());
    }

    @Test
    @DisplayName("Deve retornar pedido atualizado quando usuário é admin")
    void update_Admin() {
        setUpAsAdmin();
        Order updatedOrder = createUpdateOrder(admin, OrderStatus.WAITING_PAYMENT);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.update(1L, updatedOrder);

        assertEquals(OrderStatus.WAITING_PAYMENT, result.getOrderStatus());
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve retornar pedido atualizado quando usuário é o dono do pedido")
    void update_User() {
        Order updatedOrder = createUpdateOrder(user, OrderStatus.WAITING_PAYMENT);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.update(1L, updatedOrder);

        assertEquals(OrderStatus.WAITING_PAYMENT, result.getOrderStatus());
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve aplicar desconto quando cupom é válido")
    void update_ApplyDiscount() {
        updateOrder = createUpdateOrder(user, OrderStatus.WAITING_PAYMENT);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.update(1L, updateOrder);

        assertNotNull(result.getDiscount());
        verify(couponRepository).findById(1L);
    }

    @Test
    @DisplayName("InserirCupomINValido_AplicaDesconto - Deve lançar exceção quando cupom é inválido")
    void update_CouponIsInvalid() {
        // Coupon n persistido
        Coupon nonExistentCoupon = new Coupon();
        nonExistentCoupon.setId(999L); // ID que não existe no banco
        nonExistentCoupon.setCode("INVALID_123");
        order.setDiscount(nonExistentCoupon);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(couponRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.update(1L, order);
        });
        verify(orderRepository).findById(1L);
        verify(couponRepository).findById(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedido não existe")
    void update_OrderDoesNotExist() {
        when(orderRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.update(999L, new Order());
        });
        verify(orderRepository).findById(999L);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    @DisplayName("Deve lançar AccessDeniedException quando usuário não tem permissão")
    void update_WhenUserNotAuthorized() {
        when(orderRepository.findById(2L)).thenReturn(Optional.of(orderDois));
        when(authenticationFacade.isAdmin(any())).thenReturn(false);
        when(authenticationFacade.isSameUser(any())).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            orderService.update(2L, new Order());
        });
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"PAID", "CANCELED"})
    @DisplayName("Deve lançar exceção ao tentar alterar pedido já finalizado (PAID ou CANCELED)")
    void update_AlreadyPaidOrCanceled(OrderStatus status) {
        Order finalizedOrder = new Order();
        finalizedOrder.setId(2L);
        finalizedOrder.setOrderStatus(status);
        finalizedOrder.setClient(user);

        Order updateData = new Order();
        updateData.setOrderStatus(OrderStatus.WAITING_PAYMENT);

        when(orderRepository.findById(2L)).thenReturn(Optional.of(finalizedOrder));
        when(authenticationFacade.isAdmin(any())).thenReturn(true); // Assume admin para simplificar

        assertThrows(OrderStatusConflictException.class,
                () -> orderService.update(finalizedOrder.getId(), updateData));

        verify(orderRepository).findById(2L);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(couponRepository, userRepository);
    }

    @Test
    @DisplayName("Deve atualizar os itens de um pedido com sucesso")
    void updateItens() {
        OrderItem orderItem = createOrderItem();
        Set<OrderItem> novosItens = Set.of(orderItem);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findById(orderItem.getProduct().getId()))
                .thenReturn(Optional.of(orderItem.getProduct()));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.updateOrderItems(1L, novosItens, SecurityContextHolder.getContext().getAuthentication());

        assertEquals(1, result.getItems().size());
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Deve atualizar os itens de um pedido com sucesso, com usuário ADMIN")
    void updateItens_Admin() {
        setUpAsAdmin();
        OrderItem orderItem = createOrderItem();
        Set<OrderItem> novosItens = Set.of(orderItem);

        when(orderRepository.findById(2L)).thenReturn(Optional.of(orderDois));
        when(productRepository.findById(orderItem.getProduct().getId()))
                .thenReturn(Optional.of(orderItem.getProduct()));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.updateOrderItems(2L, novosItens, SecurityContextHolder.getContext().getAuthentication());

        assertEquals(1, result.getItems().size());
        verify(orderRepository).save(orderDois);
    }

    @Test
    @DisplayName("Deve retornar ResourceNotFoundException quando o pedido nao é encontrado")
    void updateItens_WhenOrderNotFound() {
        OrderItem orderItem = createOrderItem();
        Set<OrderItem> novosItens = Set.of(orderItem);
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.updateOrderItems(1L, novosItens, SecurityContextHolder.getContext().getAuthentication());
        });

        verify(orderRepository).findById(1L);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    @DisplayName("Deve retornar ResourceNotFoundException quando o produto não é encontrado")
    void update_WhenProductNotFound() {
        Long nonExistentProductId = 99L;
        Product invalidProduct = new Product();
        invalidProduct.setId(nonExistentProductId);

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(invalidProduct);
        orderItem.setQuantity(1);
        Set<OrderItem> novosItens = Set.of(orderItem);

        Order existingOrder = new Order();
        existingOrder.setId(1L);
        existingOrder.setOrderStatus(OrderStatus.WAITING_PAYMENT);
        existingOrder.setClient(user);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
        when(productRepository.findById(nonExistentProductId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            orderService.updateOrderItems(1L, novosItens, SecurityContextHolder.getContext().getAuthentication());
        });

        assertEquals("Produto não encontrado: ID " + nonExistentProductId, exception.getMessage());
        verify(productRepository).findById(nonExistentProductId);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"PAID", "CANCELED"})
    @DisplayName("Deve lançar exceção ao tentar alterar itens de pedido já finalizado (PAID ou CANCELED)")
    void updateOrderItems_AlreadyFinalized(OrderStatus status) {
        Order finalizedOrder = new Order();
        finalizedOrder.setId(2L);
        finalizedOrder.setOrderStatus(status);
        finalizedOrder.setClient(user);

        Product product = new Product();
        product.setId(1L);
        OrderItem newItem = new OrderItem();
        newItem.setProduct(product);
        newItem.setQuantity(2);
        Set<OrderItem> newItems = Set.of(newItem);

        when(orderRepository.findById(2L)).thenReturn(Optional.of(finalizedOrder));
        lenient().when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act & Assert
        assertThrows(OrderStatusConflictException.class,
                () -> orderService.updateOrderItems(2L, newItems, SecurityContextHolder.getContext().getAuthentication()));

        // Verificações
        verify(orderRepository).findById(2L);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(couponRepository, userRepository);

        // Opcional: verificar se não chegou a tentar atualizar os itens
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve remover produto de um order com sucesso")
    void removeProduct() {
        // Arrange
        order.getItems().add(orderItem);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        Order resultado = orderService.removeProductFromOrder(order.getId(), product.getId());

        // Assert
        assertTrue(resultado.getItems().isEmpty());
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Deve remover produto de um order com sucesso, quando o usuário é admin")
    void removeProduct_admin() {
        setUpAsAdmin();
        // Arrange
        order.getItems().add(orderItem);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        Order resultado = orderService.removeProductFromOrder(order.getId(), product.getId());

        // Assert
        assertTrue(resultado.getItems().isEmpty());
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Deve lançar Resource Not Found quando não encontrar o produto no pedido")
    void removeProduct_ProdutoNaoEncontrado() {
        // Arrange
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.removeProductFromOrder(order.getId(), product.getId());
        });
    }

    @Test
    @DisplayName("Deve lançar Resource Not Found quando não encontrar o pedido")
    void removeProduct_PedidoNaoEncontrado() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.removeProductFromOrder(999L, product.getId());
        });
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"PAID", "CANCELED"})
    @DisplayName("Deve lançar exceção ao tentar alterar itens de pedido já finalizado (PAID ou CANCELED)")
    void deveLancarExcecao_QuandoPedidoFinalizado(OrderStatus statusFinalizado) {
        // Arrange
        order.setOrderStatus(statusFinalizado);
        order.getItems().add(orderItem);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(OrderStatusConflictException.class, () -> {
            orderService.removeProductFromOrder(order.getId(), product.getId());
        });
    }

    @Test
    void upadteStatus() {
        order.setOrderStatus(OrderStatus.WAITING_PAYMENT);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order resultado = orderService.updateOrderStatus(order.getId(), OrderStatus.PAID);

        assertEquals(OrderStatus.PAID, resultado.getOrderStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void upadteStatus_admin() {
        setUpAsAdmin();
        order.setOrderStatus(OrderStatus.WAITING_PAYMENT);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order resultado = orderService.updateOrderStatus(order.getId(), OrderStatus.PAID);

        assertEquals(OrderStatus.PAID, resultado.getOrderStatus());
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Deve lancar AccessDeniedException quando o usuario nao é dono do pedido")
    void upadteStatusAccessDenied() {
        when(orderRepository.findById(orderDois.getId())).thenReturn(Optional.of(orderDois));
        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            orderService.updateOrderStatus(orderDois.getId(), OrderStatus.PAID);
        });
    }


    @Test
    void upadteStatusPedidoNaoEncontrado() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.updateOrderStatus(99L, OrderStatus.PAID);
        });
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"PAID", "CANCELED"})
    void upadteStatusPedidoFinalizado(OrderStatus statusFinalizado) {
        // Arrange
        order.setOrderStatus(statusFinalizado);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(OrderStatusConflictException.class, () -> {
            orderService.updateOrderStatus(order.getId(), OrderStatus.WAITING_PAYMENT);
        });
    }

    @Test
    @DisplayName("Deve deletar o pedido com sucesso quando usuário tem permissão")
    void delete() {
        setUpAsAdmin();
        order.getItems().add(orderItem);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        assertDoesNotThrow(() -> orderService.delete(order.getId()));
        verify(orderRepository).save(order);
        verify(orderRepository).deleteById(order.getId());
    }

    @Test
    @DisplayName("Deve lançar DatabaseException se ocorrer violação de integridade ao deletar o pedido")
    void deleteDatabaseException() {
        setUpAsAdmin();
        order.getItems().add(orderItem);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        doThrow(DataIntegrityViolationException.class).when(orderRepository).deleteById(order.getId());

        assertThrows(DatabaseException.class, () -> {
            orderService.delete(order.getId());
        });
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando pedido não for encontrado")
    void deletePedidoNaoEncontrado() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.delete(999L);
        });

        verify(orderRepository, never()).deleteById(anyLong());
    }







}

