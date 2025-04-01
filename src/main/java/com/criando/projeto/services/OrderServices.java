package com.criando.projeto.services;

import com.criando.projeto.entities.*;
import com.criando.projeto.entities.enums.OrderStatus;
import com.criando.projeto.queryFIlters.OrderQueryFilter;
import com.criando.projeto.repositories.*;
import com.criando.projeto.services.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


//@Component registra a classe como componente, para que ela possa ser injetado automaticamente com o AutoWired
// Mas tem uma anotação que faz a mesma coisa, só que é semanticamente mais correta:
@Service
public class OrderServices {


    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthenticationFacade authenticationFacade; // Para pegar o usuário logado



    public List<Order> findOrders(OrderQueryFilter filter) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Se o usuário for do role USER, ele só pode ver os próprios pedidos
        if (authenticationFacade.isUser(authentication)) {
            // Define automaticamente o userId para o id do usuário logado
            Long userId = authenticationFacade.getAuthenticatedUser().getId();
            filter.setUserId(userId);
        }
        // Gera a Specification a partir do filtro
        Specification<Order> spec = filter.toSpecification();
        // Busca os pedidos com base na Specification
        return orderRepository.findAll(spec);
    }


    public Order findById(Long id, Authentication authentication) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado" + id));
        if (!authenticationFacade.isAdmin(authentication) && !authenticationFacade.isSameUser(order.getClient().getId())) {
            throw new AccessDeniedException("Você não tem permissão para acessar este pedido.");
        }
        return order;
    }



    public Order insert(Order order) {
        String email = authenticationFacade.getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado" + email));
        order.setClient(user);
        // Guarda os itens enviados na requisição
        var orderItems = order.getItems();
        // Zera os itens do pedido para evitar problemas de persistência, Quando você "zera" os itens do pedido com order.setItems(new HashSet<>()), a referência antiga para os itens é substituída, mas os objetos anteriores ainda existem na memória até que o Garbage Collector (GC) do Java os remova, caso não estejam mais sendo referenciados.
        order.setItems(new HashSet<>());
        // Se o pedido tiver um cupom, buscar no banco de dados
        Coupon coupon = applyCouponToOrder(order);
        if (coupon != null) {
            order.setDiscount(coupon);
        }
        // Salva o pedido sem itens
        var savedOrder = orderRepository.save(order);
        // Processa cada item para buscar o produto e definir corretamente o preço
        orderItems.forEach(orderItem -> {
            // Buscar o produto no banco de dados
            Product product = productRepository.findById(orderItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: ID " + orderItem.getProduct().getId()));
            // Atualizar os dados do OrderItem com os dados do produto encontrado, de acordo com os atributos do order item
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setPrice(product.getPrice()); // Define o preço do produto no pedido
        });
        // Salva todos os itens com as informações do produto
        var savedItems = orderItemRepository.saveAll(orderItems);
        // Associa os itens ao pedido
        savedOrder.setItems(new HashSet<>(savedItems));
        return savedOrder;
    }


    public Order setOrderPayment(Long orderId, Payment payment) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado. ID:" + orderId));
        //verifica se o status é = a PAID ou CANCELED
        validateOrderStatus(order);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authenticationFacade.isAdmin(authentication) && !authenticationFacade.isSameUser(order.getClient().getId())) {
            throw new AccessDeniedException("Você não tem permissão para alterar o pagamento deste pedido.");
        }
        if (order.getOrderStatus() == null || !isValidOrderStatus(order.getOrderStatus())) {
            throw new InvalidOrderStatusException(order.getOrderStatus().toString(), "O status fornecido não é válido.");
        }
        payment.setOrder(order); //Order tem uma referência para Payment.
        order.setPayment(payment); //Payment tem uma referência para Order.
        // Verifica se o pagamento está preenchido e, se sim, altera o status do pedido para "PAID"
        if (payment.getPaymentMethod() != null) {
            // Considerando que o pagamento foi concluído se tiver valor e data de pagamento
            order.setOrderStatus(OrderStatus.PAID); // Ou outro status como "COMPLETED"
        }
        // Salva o pedido com os dados atualizados
        return orderRepository.save(order);
    }


    public Order setOrDeleteCoupon(Long orderId, Long couponId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado. ID:" + orderId));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authenticationFacade.isAdmin(authentication) && !authenticationFacade.isSameUser(order.getClient().getId())) {
            throw new AccessDeniedException("Você não tem permissão para aplicar/remover cupom deste pedido.");
        }
        validateOrderStatus(order);
        // Se o couponId não for nulo, buscar o cupom no banco de dados
        if (couponId != null) {
            Coupon coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cupom não encontrado: ID " + couponId));
            // Verificar se o cupom já foi aplicado, caso contrário, aplicar o novo cupom
            if (order.getDiscount() != null && order.getDiscount().getId().equals(couponId)) {
                throw new CouponAlreadyAppliedException(couponId);
            }
            order.setDiscount(coupon); // Associar o cupom ao pedido
        } else {
            // Se couponId for nulo, remover o cupom
            order.setDiscount(null);
        }
        // Salvar o pedido com as alterações
        return orderRepository.save(order);
    }



    public Order update(Long id, Order obj) {
        try {
            Order entity = orderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado. ID:" + id));
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!authenticationFacade.isAdmin(authentication) && !authenticationFacade.isSameUser(entity.getClient().getId())) {
                throw new AccessDeniedException("Você não tem permissão para atualizar este pedido.");
            }
            validateOrderStatus(entity);
            updateData(entity, obj);
            return orderRepository.save(entity);
        } catch (ResourceNotFoundException e) {
            throw e;
        }
    }

    private void updateData(Order entity, Order obj) {
        // Atualiza o status do pedido
        if (obj.getOrderStatus() != null) {
            if (!isValidOrderStatus(obj.getOrderStatus())) {
                throw new InvalidOrderStatusException(obj.getOrderStatus().toString(), "O status fornecido não é válido.");
            }
            entity.setOrderStatus(obj.getOrderStatus());
        }
        // Atualiza o cliente do pedido, buscando no banco se necessário
        if (obj.getClient() != null && obj.getClient().getId() != null) {
            User client = userRepository.findById(obj.getClient().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: ID " + obj.getClient().getId()));
            entity.setClient(client);
        }
        if (obj.getDiscount() != null && obj.getDiscount().getId() != null) {
            Coupon coupon = couponRepository.findById(obj.getDiscount().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cupom não encontrado: ID " + obj.getDiscount().getId()));
            entity.setDiscount(coupon); // Atualiza o cupom no pedido
        } else {
            entity.setDiscount(null); // Se não passar um cupom, limpa o cupom
        }
    }



    public Order updateOrderItems(Long orderId, Set<OrderItem> newItems, Authentication authentication) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado. ID:" + orderId));
        if (!authenticationFacade.isAdmin(authentication) && !authenticationFacade.isSameUser(order.getClient().getId())) {
            throw new AccessDeniedException("Você não tem permissão para editar este pedido.");
        }
        validateOrderStatus(order);
        updateItemsInOrder(order, newItems);
        return orderRepository.save(order);
    }

    // Metodo responsável por atualizar ou adicionar itens ao pedido
    private void updateItemsInOrder(Order order, Set<OrderItem> newItems) {
        // Mapeia os itens existentes do pedido para um Map, usando o ID do produto como chave
        Map<Long, OrderItem> existingItemsMap = order.getItems().stream()
                .collect(Collectors.toMap(item -> item.getProduct().getId(), item -> item));
        for (OrderItem newItem : newItems) {
            OrderItem existingItem = existingItemsMap.get(newItem.getProduct().getId());
            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + newItem.getQuantity());
            } else {
                addNewItemToOrder(order, newItem);
            }
        }
    }

    // Metodo auxiliar para adicionar um novo item ao pedido
    private void addNewItemToOrder(Order order, OrderItem newItem) {
        // Busca o produto no banco para garantir que estamos associando um produto válido
        Product product = productRepository.findById(newItem.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: ID " + newItem.getProduct().getId()));
        newItem.setOrder(order);
        newItem.setProduct(product);
        newItem.setPrice(product.getPrice());
        order.getItems().add(newItem);
    }




    public Order updateOrderStatus(Long id, OrderStatus status) {
        Order entity = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado. ID:" + id));;
        validateOrderStatus(entity);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authenticationFacade.isAdmin(authentication) && !authenticationFacade.isSameUser(entity.getClient().getId())) {
            throw new AccessDeniedException("Você não tem permissão para atualizar o status deste pedido.");
        }
        if (status == null || !isValidOrderStatus(status)) {
            throw new InvalidOrderStatusException(status.toString(), "O status fornecido não é válido.");
        }
        entity.setOrderStatus(status);
        return orderRepository.save(entity);
    }

    public Order removeProductFromOrder(Long orderId, Long productId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado. ID:" + orderId));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authenticationFacade.isAdmin(authentication) && !authenticationFacade.isSameUser(order.getClient().getId())) {
            throw new AccessDeniedException("Você não tem permissão para remover este produto do pedido.");
        }
        validateOrderStatus(order);
        // Buscar o item do pedido que possui o produto com o ID fornecido
        OrderItem itemToRemove = order.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado no pedido"));
        order.getItems().remove(itemToRemove);
        return orderRepository.save(order);
    }

    public void delete(Long id) {
        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado. ID:" + id));
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!authenticationFacade.isAdmin(authentication) && !authenticationFacade.isSameUser(order.getClient().getId())) {
                throw new AccessDeniedException("Você não tem permissão para deletar este pedido.");
            }
            if (order.getPayment() != null) {
                order.setPayment(null);
            }
            order.setDiscount(null);
            order.getItems().clear();
            orderRepository.save(order);
            orderRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    private Coupon applyCouponToOrder(Order order) {
        if (order == null) {
            throw new ResourceNotFoundException("Pedido não encontrado.");
        }
        if (order.getDiscount() != null && order.getDiscount().getId() != null) {
            return couponRepository.findById(order.getDiscount().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cupom não encontrado: ID " + order.getDiscount().getId()));
        }
        return null;
    }


    // Método auxiliar para verificar se o status é válido
    private boolean isValidOrderStatus(OrderStatus status) {
        return status == OrderStatus.WAITING_PAYMENT || status == OrderStatus.PAID || status == OrderStatus.CANCELED;
    }
    // Método auxiliar para verificar se o pedido está finalizado no sistema
    private void validateOrderStatus(Order order) {
        if (order.getOrderStatus() == OrderStatus.PAID || order.getOrderStatus() == OrderStatus.CANCELED) {
            throw new OrderStatusConflictException("Não é possível atualizar o pedido com status 'PAID' ou 'CANCELED'");
        }
    }
}
