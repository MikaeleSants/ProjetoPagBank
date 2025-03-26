package com.criando.projeto.services.exceptions;

public class OrderStatusConflictException extends RuntimeException {
    public OrderStatusConflictException(String message) {
        super(message);
    }
}
