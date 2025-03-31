package com.criando.projeto.services.exceptions;

public class InvalidPasswordPatternException extends RuntimeException {
    public InvalidPasswordPatternException() {
        super("A senha deve conter pelo menos uma letra, um número e um caractere especial.");
    }
}