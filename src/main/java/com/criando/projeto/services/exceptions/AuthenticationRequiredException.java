package com.criando.projeto.services.exceptions;

public class AuthenticationRequiredException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AuthenticationRequiredException() {
        super("Nenhum usuário autenticado encontrado");
    }

    public AuthenticationRequiredException(String message) {
        super(message);
    }
}
