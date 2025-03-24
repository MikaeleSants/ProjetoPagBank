package com.criando.projeto.services;

import com.criando.projeto.entities.*;
import com.criando.projeto.entities.enums.OrderStatus;
import com.criando.projeto.repositories.*;
import com.criando.projeto.services.exceptions.CouponAlreadyAppliedException;
import com.criando.projeto.services.exceptions.DatabaseException;
import com.criando.projeto.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.domain.Specification;
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

    //injeta a Orderrepository, mas não precisamos botar o @Component na classe OrderRepository
    //como fizemos nessa, pq o OrderRepository extends JpaRepository, que já é marcado como componente
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

    public List<Order> findAll(Specification<Order> specification) {
        return orderRepository.findAll(specification);}

    public Order findById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }

    public Order insert(Order order) {
        // Guarda os itens enviados na requisição
        var orderItems = order.getItems();

        // Zera os itens do pedido para evitar problemas de persistência
        order.setItems(new HashSet<>());

        // Se o pedido tiver um cupom, buscar no banco de dados
        if (order.getDiscount() != null && order.getDiscount().getId() != null) {
            Coupon coupon = couponRepository.findById(order.getDiscount().getId())
                    .orElseThrow(() -> new RuntimeException("Cupom não encontrado: ID " + order.getDiscount().getId()));
            order.setDiscount(coupon);
        }

        // Salva o pedido sem itens
        var savedOrder = orderRepository.save(order);

        // Processa cada item para buscar o produto e definir corretamente o preço
        orderItems.forEach(orderItem -> {
            // Buscar o produto no banco de dados
            Product product = productRepository.findById(orderItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado: ID " + orderItem.getProduct().getId()));

            // Atualizar os dados do OrderItem com os dados do produto encontrado
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
        // Buscar o pedido pelo ID
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(orderId));
        if (order.getOrderStatus() == OrderStatus.PAID || order.getOrderStatus() == OrderStatus.CANCELED) {
            throw new IllegalStateException("Não é possível atualizar o pedido com status 'PAID' ou 'CANCELED'");
        }

        // Atualiza os dados do pagamento e associa ao pedido
        payment.setOrder(order);
        order.setPayment(payment);

        // Verifica se o pagamento está preenchido e, se sim, altera o status do pedido para "PAID"
        if (payment.getPaymentMethod() != null) {
            // Considerando que o pagamento foi concluído se tiver valor e data de pagamento
            order.setOrderStatus(OrderStatus.PAID); // Ou outro status como "COMPLETED"
        }
        // Salva o pedido com os dados atualizados
        return orderRepository.save(order);
    }

    public Order setCoupon(Long orderId, Long couponId) {
        // Buscar o pedido pelo ID
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(orderId));

        if (order.getOrderStatus() == OrderStatus.PAID || order.getOrderStatus() == OrderStatus.CANCELED) {
            throw new IllegalStateException("Não é possível atualizar o pedido com status 'PAID' ou 'CANCELED'");
        }

        // Se o couponId não for nulo, buscar o cupom no banco de dados
        if (couponId != null) {
            Coupon coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new ResourceNotFoundException(couponId));

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
            // Buscar o pedido pelo ID
            Order entity = orderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(id)); // Lança 404 se não encontrar

            if (entity.getOrderStatus() == OrderStatus.PAID || entity.getOrderStatus() == OrderStatus.CANCELED) {
                throw new IllegalStateException("Não é possível atualizar o pedido com status 'PAID' ou 'CANCELED'");
            }
            // Atualizar os dados gerais do pedido
            updateData(entity, obj);

            // Salvar o pedido com as atualizações
            return orderRepository.save(entity);
        } catch (ResourceNotFoundException e) {
            throw e; // Garante que o erro 404 seja propagado corretamente
        }
    }

    private void updateData(Order entity, Order obj) {

        // Atualiza o status do pedido
        if (obj.getOrderStatus() != null) {
            entity.setOrderStatus(obj.getOrderStatus());
        }

        // Atualiza o cliente do pedido, buscando no banco se necessário
        if (obj.getClient() != null && obj.getClient().getId() != null) {
            User client = userRepository.findById(obj.getClient().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: ID " + obj.getClient().getId()));
            entity.setClient(client);
        }

        /* Atualiza o pagamento do pedido, buscando no banco se necessário
        if (obj.getPayment() != null && obj.getPayment().getId() != null) {
            Payment payment = paymentRepository.findById(obj.getPayment().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado: ID " + obj.getPayment().getId()));
            entity.setPayment(payment);
        }*/

        // Atualiza o cupom, se for passado, buscando no banco
        if (obj.getDiscount() != null && obj.getDiscount().getId() != null) {
            Coupon coupon = couponRepository.findById(obj.getDiscount().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cupom não encontrado: ID " + obj.getDiscount().getId()));
            entity.setDiscount(coupon);
        } else {
            // Se o cupom for nulo, remove o cupom
            entity.setDiscount(null);
        }
    }

    // Atualizar ou adicionar itens ao pedido
    public Order updateOrderItems(Long orderId, Set<OrderItem> newItems) {
        // Buscar o pedido pelo ID
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(orderId));
        if (order.getOrderStatus() == OrderStatus.PAID || order.getOrderStatus() == OrderStatus.CANCELED) {
            throw new IllegalStateException("Não é possível atualizar o pedido com status 'PAID' ou 'CANCELED'");
        }

        // Atualizar ou adicionar os itens ao pedido
        updateItemsInOrder(order, newItems);

        // Salvar o pedido com os itens atualizados ou adicionados
        return orderRepository.save(order);
    }

    // Metodo responsável por atualizar ou adicionar itens ao pedido
    private void updateItemsInOrder(Order order, Set<OrderItem> newItems) {
        // Mapeia os itens existentes do pedido para um Map, usando o ID do produto como chave
        Map<Long, OrderItem> existingItemsMap = order.getItems().stream()
                .collect(Collectors.toMap(item -> item.getProduct().getId(), item -> item));

        for (OrderItem newItem : newItems) {
            // Buscar ou atualizar item
            OrderItem existingItem = existingItemsMap.get(newItem.getProduct().getId());

            if (existingItem != null) {
                // Atualiza a quantidade do item existente e mantém o preço original
                existingItem.setQuantity(existingItem.getQuantity() + newItem.getQuantity());
            } else {
                // Caso o item não exista, associamos o produto e adicionamos ao pedido
                addNewItemToOrder(order, newItem);
            }
        }
    }

    // Metodo auxiliar para adicionar um novo item ao pedido
    private void addNewItemToOrder(Order order, OrderItem newItem) {
        // Busca o produto no banco para garantir que estamos associando um produto válido
        Product product = productRepository.findById(newItem.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: ID " + newItem.getProduct().getId()));

        // Define as informações do novo item
        newItem.setOrder(order);         // Associa o item ao pedido
        newItem.setProduct(product);     // Associa o produto ao item
        newItem.setPrice(product.getPrice());  // Define o preço do produto no pedido

        // Adiciona o item à lista de itens do pedido
        order.getItems().add(newItem);
    }


    public Order updateOrderStatus(Long id, OrderStatus status) {
        Order entity = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
        if (entity.getOrderStatus() == OrderStatus.PAID || entity.getOrderStatus() == OrderStatus.CANCELED) {
            throw new IllegalStateException("Não é possível atualizar o pedido com status 'PAID' ou 'CANCELED'");
        }

        entity.setOrderStatus(status);
        return orderRepository.save(entity);
    }

    public Order removeProductFromOrder(Long orderId, Long productId) {
        // Buscar o pedido pelo ID
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(orderId));
        if (order.getOrderStatus() == OrderStatus.PAID || order.getOrderStatus() == OrderStatus.CANCELED) {
            throw new IllegalStateException("Não é possível atualizar o pedido com status 'PAID' ou 'CANCELED'");
        }

        // Buscar o item do pedido que possui o produto com o ID fornecido
        OrderItem itemToRemove = order.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado no pedido"));

        // Remover o item do pedido
        order.getItems().remove(itemToRemove);

        // Salvar o pedido atualizado
        return orderRepository.save(order);
    }

    public void delete(Long id) {

        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(id));

            // Remove a referência ao pagamento (se existir)
            if (order.getPayment() != null) {
                order.setPayment(null);
            }

            // Remove a referência ao cupom (se existir)
            order.setDiscount(null);

            // Limpa os itens do pedido
            order.getItems().clear();
            orderRepository.save(order); // Salva a ordem sem itens antes de excluir

            orderRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

}
