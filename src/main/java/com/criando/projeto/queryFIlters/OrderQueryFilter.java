package com.criando.projeto.queryFIlters;
import com.criando.projeto.entities.Order;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import static com.criando.projeto.specifications.OrderSpec.*;

@Data
public class OrderQueryFilter {

    private String orderStatus;
    private Long userId;

    public Specification<Order> toSpecification() {
        Specification<Order> spec = Specification.where(null);

        if (userId != null) {
            spec = spec.and(byUserId(userId));
        }

        if (orderStatus != null && !orderStatus.isEmpty()) {
            spec = spec.and(orderStatusEquals(orderStatus));
        }

        return spec;
    }
}
