package com.criando.projeto.services;

import com.criando.projeto.entities.Coupon;
import com.criando.projeto.entities.Order;
import com.criando.projeto.repositories.CouponRepository;
import com.criando.projeto.repositories.OrderRepository;
import com.criando.projeto.services.exceptions.DatabaseException;
import com.criando.projeto.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;


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

    public List<Order> findAll(Specification<Order> spec) {
        return orderRepository.findAll(spec);}

    public Order findById(Long id) {
        Optional <Order> obj =  orderRepository.findById(id);
        return obj.get();
    }

    public Order insert(Order obj) {
        return orderRepository.save(obj);
    }

    public void delete(Long id) {
        try {
            orderRepository.deleteById(id); } catch (EmptyResultDataAccessException e)
        {throw new ResourceNotFoundException(id);} catch (DataIntegrityViolationException e)
        {throw new DatabaseException(e.getMessage());}
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
        entity.setItems(obj.getItems());
        entity.setPayment(obj.getPayment());
        if (obj.getDiscount() != null) {
            entity.setDiscount(obj.getDiscount());
        } else {
            entity.setDiscount(null);
        }
    }

    public Order applyCouponToOrder(Long orderId, Long couponId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(orderId));

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException(couponId));

        order.applyCoupon(coupon);
        return orderRepository.save(order);
    }
}
