package com.criando.projeto.services.exceptions;

public class NullPasswordException extends RuntimeException {
    public NullPasswordException() {
        super("A senha não pode ser nula.");
    }
}