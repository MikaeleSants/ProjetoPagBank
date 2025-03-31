package com.criando.projeto.specifications;

import com.criando.projeto.entities.Order;
import com.criando.projeto.entities.enums.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpec {

    // Filtro por status (aceita nome ou código)
    public static Specification<Order> orderStatusEquals(String status) {
        return (root, query, builder) -> {
            if (status == null || status.isEmpty()) {
                return null;
            }

            try {
                // Tenta interpretar como nome da enum (ex: "PAID")
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                return builder.equal(root.get("orderStatus"), orderStatus.getCode());
            } catch (IllegalArgumentException e) {
                try {
                    // Tenta interpretar como código numérico (ex: "1", "2")
                    int code = Integer.parseInt(status);
                    return builder.equal(root.get("orderStatus"), code);
                } catch (NumberFormatException ex) {
                    return null; // Se não for nem nome nem número válido, ignora o filtro
                }
            }
        };
    }

    // Filtro para buscar pedidos por ID do usuário (cliente)
    public static Specification<Order> byUserId(Long userId) {
        return (root, query, builder) -> {
            if (userId == null) {
                return null; // Retorna sem aplicar filtro se userId for null
            }
            return builder.equal(root.get("client").get("id"), userId);
        };
    }
}
