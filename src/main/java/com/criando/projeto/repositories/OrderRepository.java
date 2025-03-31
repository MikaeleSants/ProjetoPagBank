package com.criando.projeto.repositories;

import com.criando.projeto.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    boolean existsByIdAndClient_Email(Long id, String email);
}
