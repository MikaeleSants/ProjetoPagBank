package com.criando.projeto.services.exceptions;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("Pedido com ID " + id + " não encontrado");
    }
}
