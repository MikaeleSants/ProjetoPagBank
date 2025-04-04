package com.criando.projeto.services.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(Object id) {
        super("Busca incompleta! id: " + id + " não encontrado!");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
