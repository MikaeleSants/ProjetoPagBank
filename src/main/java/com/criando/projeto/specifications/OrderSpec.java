package com.criando.projeto.specifications;

import com.criando.projeto.entities.Order;
import com.criando.projeto.entities.Product;
import com.criando.projeto.entities.enums.OrderStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

public class OrderSpec {

    // Método que mapeia o nome do status para o valor da enum OrderStatus
    public static Specification<Order> orderStatusEquals(String status) {
        return (root, query, builder) -> {
            if (status == null || status.isEmpty()) {
                return null; // Se o status for null ou vazio, não aplica filtro
            }
            try {
                // Convertendo o nome do status para a enum OrderStatus
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                // Retorna a condição de filtro para o status
                return builder.equal(root.get("orderStatus"), orderStatus.getCode());
            } catch (IllegalArgumentException e) {
                return null; // Retorna sem aplicar filtro se o status for inválido
            }
        };
    }

    // Filtro para buscar pedidos por userId (cliente)
    public static Specification<Order> byUserId(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction(); // Retorna todos os pedidos se userId for null
            }
            return criteriaBuilder.equal(root.get("client").get("id"), userId); // Filtro por id do cliente
        };
    }

    // Filtro para buscar pedidos por OrderStatus (como enum)
    public static Specification<Order> byOrderStatus(OrderStatus orderStatus) {
        return (root, query, criteriaBuilder) -> {
            if (orderStatus == null) {
                return criteriaBuilder.conjunction(); // Retorna todos os pedidos se orderStatus for null
            }
            return criteriaBuilder.equal(root.get("orderStatus"), orderStatus.getCode()); // Filtro por status do pedido
        };
    }
}
