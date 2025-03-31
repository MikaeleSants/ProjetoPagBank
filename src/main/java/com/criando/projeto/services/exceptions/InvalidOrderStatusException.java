package com.criando.projeto.services.exceptions;

public class InvalidOrderStatusException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidOrderStatusException(String message) {
        super(message);
    }

    public InvalidOrderStatusException(String status, String message) {
        super("Status inválido: " + status + ". " + message);
    }
}
