package com.criando.projeto.security;

import com.criando.projeto.entities.Order;
import com.criando.projeto.entities.User;
import com.criando.projeto.repositories.OrderRepository;
import com.criando.projeto.repositories.UserRepository;
import com.criando.projeto.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class OrderSecurity {

    private final OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    public OrderSecurity(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // Verifica se o usuário autenticado é o proprietário do pedido
    public boolean checkOrderOwnership(Authentication authentication, Long orderId) {
        // Obtém o e-mail do usuário autenticado
        String email = authentication.getName();

        // Busca o usuário pelo e-mail
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Verifica se o pedido existe
        Order order = orderRepository.findById(orderId).orElse(null);

        // Se o pedido não existir, retorna false
        if (order == null) {
            return false;
        }

        // Se for ADMIN, permite o acesso
        if ("ADMIN".equals(user.getRole().name())) {
            return true; }

            // Permite apenas se o ID do usuário autenticado for igual ao ID do cliente associado ao pedido
            return order.getClient().getId().equals(user.getId());
    }

}