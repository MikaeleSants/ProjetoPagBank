package com.criando.projeto.repositories;

import com.criando.projeto.entities.OrderItem;
import com.criando.projeto.entities.pk.OrderItemPk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemPk> {
}
