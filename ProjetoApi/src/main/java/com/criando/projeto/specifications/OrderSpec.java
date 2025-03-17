package com.criando.projeto.specifications;

import com.criando.projeto.entities.Order;
import com.criando.projeto.entities.Product;
import com.criando.projeto.entities.enums.OrderStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

public class OrderSpec {

    // Metodo que mapeia o nome do status para o valor da enum OrderStatus
    public static Specification<Order> orderStatusEquals(String status) {
        return (root, query, builder) -> {
            if (status == null || status.isEmpty()) {
                return null;
            }

            // Convertendo o nome do status para a enum OrderStatus
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());

            // Retorna a condição de filtro para o status
            return builder.equal(root.get("orderStatus"), orderStatus.getCode());
        };
    }

}
