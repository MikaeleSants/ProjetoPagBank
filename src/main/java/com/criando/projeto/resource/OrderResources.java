package com.criando.projeto.resource;

import com.criando.projeto.entities.Order;
import com.criando.projeto.entities.OrderItem;
import com.criando.projeto.entities.Payment;
import com.criando.projeto.entities.enums.OrderStatus;
import com.criando.projeto.queryFIlters.OrderQueryFilter;
import com.criando.projeto.resource.exceptions.ValidationError;
import com.criando.projeto.services.OrderServices;
import com.criando.projeto.services.exceptions.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag(name = "Pedidos", description = "Endpoints relacionados à gestão de pedidos")
@RestController
@RequestMapping(value = "/orders")
public class OrderResources {
    @Autowired
    private OrderServices orderServices;



    @GetMapping
    @Operation(summary = "Buscar pedidos com filtros opcionais", description = "Retorna todos os pedidos conforme os filtros passados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de pedidos retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "422", description = "Erro de validação nos dados enviados", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationError.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<List<Order>> findOrders(OrderQueryFilter filter) {
        List<Order> orders = orderServices.findOrders(filter);
        return ResponseEntity.ok().body(orders);
    }
    /*
    GET /orders
    GET /orders?userId=1
    GET /orders?orderStatus=PAID (para user)
    GET /orders?PAID (admin)
     */


    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID", description = "Retorna um pedido específico pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Order> getOrderById(@PathVariable Long id, Authentication authentication) {
        Order order = orderServices.findById(id, authentication);
        return ResponseEntity.ok(order);
    }



    @PostMapping
    @Operation(summary = "Criar novo pedido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "422", description = "Erro de validação nos dados enviados", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationError.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Order> insert(@Valid @RequestBody Order obj) {
        obj = orderServices.insert(obj);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getId()).toUri();
        return ResponseEntity.created(uri).body(obj);
    }

    @PostMapping("/{orderId}/apply-coupon/{couponId}")
    @Operation(summary = "Aplicar cupom a um pedido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cupom aplicado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pedido ou cupom não encontrado"),
            @ApiResponse(responseCode = "409", description = "Cupom já foi aplicado ou pedido não pode ser alterado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationError.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
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
    @Operation(summary = "Atualizar pagamento de um pedido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagamento atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor"),
            @ApiResponse(responseCode = "409", description = "Pedido já foi pago ou cancelado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationError.class)))
    })
    public ResponseEntity<Order> updateOrderPayment(@PathVariable Long id, @RequestBody Payment payment) {
        Order updatedOrder = orderServices.setOrderPayment(id, payment);
        return ResponseEntity.ok(updatedOrder);
    }



    @PutMapping(value = "/{id}")
    @Operation(summary = "Atualizar pedido por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido atualizado com sucesso"),
            @ApiResponse(responseCode = "422", description = "Erro de validação nos dados enviados", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationError.class))),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "409", description = "Pedido não pode ser alterado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationError.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Order> update(@PathVariable Long id, @Valid @RequestBody Order obj) {
        obj = orderServices.update(id, obj);
        return ResponseEntity.ok().body(obj);
    }



    @DeleteMapping(value = "/{id}")
    @Operation(summary = "Deletar pedido por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pedido deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Pedido não pode ser deletado pois está pago ou cancelado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderServices.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{orderId}/remove-product/{productId}")
    @Operation(summary = "Remover produto de um pedido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pedido ou produto não encontrado"),
            @ApiResponse(responseCode = "409", description = "Pedido não pode ser deletado pois está pago ou cancelado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Order> removeProductFromOrder(@PathVariable Long orderId, @PathVariable Long productId) {
        try {
            Order updatedOrder = orderServices.removeProductFromOrder(orderId, productId);
            return ResponseEntity.ok().body(updatedOrder);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{orderId}/remove-coupon")
    @Operation(summary = "Remover cupom de um pedido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cupom removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Pedido não pode ser alterado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
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
    @Operation(summary = "Atualizar status do pedido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "409", description = "Pedido não pode ter o status alterado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate) {
        String status = statusUpdate.get("orderStatus");
        Order updatedOrder = orderServices.updateOrderStatus(id, OrderStatus.valueOf(status));
        return ResponseEntity.ok(updatedOrder);
    }

    @PatchMapping("/{id}/items")
    @Operation(summary = "Atualizar itens do pedido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Itens atualizados com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "409", description = "Pedido não pode ser alterado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Order> updateOrderItems(@PathVariable Long id, @RequestBody Set<OrderItem> items) {
        // Obtém a autenticação atual (usuário logado)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Order updatedOrder = orderServices.updateOrderItems(id, items, authentication);
        return ResponseEntity.ok(updatedOrder);
    }
}