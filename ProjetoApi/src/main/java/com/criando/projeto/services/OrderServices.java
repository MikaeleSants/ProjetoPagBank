package com.criando.projeto.services;

import com.criando.projeto.entities.Coupon;
import com.criando.projeto.entities.Order;
import com.criando.projeto.entities.OrderItem;
import com.criando.projeto.repositories.CouponRepository;
import com.criando.projeto.repositories.OrderRepository;
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

    public List<Order> findAll(Specification<Order> specification) {
        return orderRepository.findAll(specification);}

    public Order findById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }

    public Order insert(Order order) {
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

    public Order update(Long id, Order obj) {
        try {
            Order entity = orderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(id)); // Lança 404 se não encontrar
            updateData(entity, obj);
            return orderRepository.save(entity);
        } catch (ResourceNotFoundException e) {
            throw e; // Garante que o erro 404 seja propagado corretamente
        }
    }
    private void updateData(Order entity, Order obj) {
        entity.setMoment(obj.getMoment());
        entity.setOrderStatus(obj.getOrderStatus());
        entity.setClient(obj.getClient());
        entity.setPayment(obj.getPayment());
        if (obj.getDiscount() != null) {
            entity.setDiscount(obj.getDiscount());
        } else {
            entity.setDiscount(null);
        }
        updateOrderItems(entity, obj);
    }

    private void updateOrderItems(Order entity, Order obj) {
        Map<Long, OrderItem> existingItemsMap = entity.getItems().stream()
                .collect(Collectors.toMap(item -> item.getProduct().getId(), item -> item));
        //buscando itens existentes
        Set<OrderItem> newItems = new HashSet<>();
        for (OrderItem newItem : obj.getItems()) {
            OrderItem existingItem = existingItemsMap.get(newItem.getProduct().getId());

            if (existingItem != null) {
                // Atualiza quantidade e preço se o item já existe
                existingItem.setQuantity(newItem.getQuantity());
                existingItem.setPrice(newItem.getPrice());
                newItems.add(existingItem);
            } else {
                // Adiciona um novo item ao pedido
                newItems.add(newItem);
            }
        }
        // Removendo itens que não existem mais na nova lista
        entity.getItems().removeIf(item -> !newItems.contains(item));
        // Adicionando os novos itens atualizados
        entity.getItems().clear();
        entity.getItems().addAll(newItems);
    }

    public Order applyCouponToOrder(Long orderId, Long couponId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(orderId));

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException(couponId));

        if (order.getDiscount() != null && order.getDiscount().getId().equals(couponId)) {
            throw new CouponAlreadyAppliedException(couponId);
        }

        order.applyCoupon(coupon);
        return orderRepository.save(order);
    }
}
