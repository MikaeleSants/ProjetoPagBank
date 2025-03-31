package com.criando.projeto.services.exceptions;

public class AccessDeniedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AccessDeniedException() {
        super("Acesso negado.");
    }

    public AccessDeniedException(String message) {
        super(message);
    }
}
