package com.criando.projeto.services.exceptions;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(Long id) {
        super("Categoria com ID " + id + " não encontrada");
    }
}
