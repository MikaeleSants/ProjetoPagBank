package com.criando.projeto.services.exceptions;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("O e-mail já está em uso: " + email);
    }
}
