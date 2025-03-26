package com.criando.projeto.resource;

import com.criando.projeto.entities.Order;
import com.criando.projeto.entities.OrderItem;
import com.criando.projeto.entities.Payment;
import com.criando.projeto.entities.enums.OrderStatus;
import com.criando.projeto.queryFIlters.OrderQueryFilter;
import com.criando.projeto.services.OrderServices;
import com.criando.projeto.services.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(value = "/orders")
public class OrderResources {
    @Autowired
    private OrderServices orderServices;

    //responseEntity é um tipo do spring para retornar respostas de requisicoes web
    @GetMapping
    public ResponseEntity<List<Order>> findAll(OrderQueryFilter filter) {
        List<Order> list = orderServices.findAll(filter.toSpecification());
        return ResponseEntity.ok().body(list);
    }

    //aqui eu vou botar na url o valor do id do usuario pra buscar, pra dizer que a minha url
    //recebe um paramentro, eu uso o que tem em ({}), em seguida botar uma annotation @PathVariable
    //ao lado da variavel do paramentro do metodo
    @GetMapping(value = "/{id}")
    public ResponseEntity<Order> findById(@PathVariable Long id) {
        Order obj = orderServices.findById(id);
        return ResponseEntity.ok().body(obj);
    }

    @PostMapping
    public ResponseEntity<Order> insert(@Valid @RequestBody Order obj) {
        obj = orderServices.insert(obj);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getId()).toUri();
        return ResponseEntity.created(uri).body(obj);
    }

    @PostMapping("/{orderId}/apply-coupon/{couponId}")
    public ResponseEntity<Order> applyCouponToOrder(
            @PathVariable Long orderId, @PathVariable Long couponId) {
        try {
            Order updatedOrder = orderServices.setOrDeleteCoupon(orderId, couponId);
            return ResponseEntity.ok().body(updatedOrder);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/payment")
    public ResponseEntity<Order> updateOrderPayment(@PathVariable Long id, @RequestBody Payment payment) {
        Order updatedOrder = orderServices.setOrderPayment(id, payment);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderServices.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<Order> update(@PathVariable Long id, @Valid @RequestBody Order obj) {
        obj = orderServices.update(id, obj);
        return ResponseEntity.ok().body(obj);
    }

    @DeleteMapping("/{orderId}/remove-product/{productId}")
    public ResponseEntity<Order> removeProductFromOrder(@PathVariable Long orderId, @PathVariable Long productId) {
        try {
            Order updatedOrder = orderServices.removeProductFromOrder(orderId, productId);
            return ResponseEntity.ok().body(updatedOrder);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @DeleteMapping("/{orderId}/remove-coupon")
    public ResponseEntity<Order> removeCouponFromOrder(@PathVariable Long orderId) {
        try {
            // Passar null para remover o cupom
            Order updatedOrder = orderServices.setOrDeleteCoupon(orderId, null);
            return ResponseEntity.ok().body(updatedOrder);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }



    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate) {
        String status = statusUpdate.get("orderStatus");
        Order updatedOrder = orderServices.updateOrderStatus(id, OrderStatus.valueOf(status));
        return ResponseEntity.ok(updatedOrder);
    }

    @PatchMapping("/{id}/items")
    public ResponseEntity<Order> updateOrderItems(@PathVariable Long id, @RequestBody Set<OrderItem> items) {
        Order updatedOrder = orderServices.updateOrderItems(id, items);
        return ResponseEntity.ok(updatedOrder);
    }
}
