package com.criando.projeto.services.exceptions;

public class InvalidPasswordLengthException extends RuntimeException {
    public InvalidPasswordLengthException() {
        super("A senha deve ter entre 3 e 8 caracteres.");
    }
}
