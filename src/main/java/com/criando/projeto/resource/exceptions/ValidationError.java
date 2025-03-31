package com.criando.projeto.resource.exceptions;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ValidationError extends StandardError {
    private static final long serialVersionUID = 1L;

    private List<FieldMessage> errors = new ArrayList<>();

    public ValidationError(Instant timestamp, Integer status, String error, String message, String path) {
        super(timestamp, status, error, message, path);
    }

    public List<FieldMessage> getErrors() {
        return errors;
    }

    public void addError(String fieldName, String message) {
        errors.add(new FieldMessage(fieldName, message));
    }

    //herda de StandardError e adiciona uma lista de erros de campo, permitindo adicionar múltiplos erros. Ela está configurada para ser usada especificamente em casos de validação, como erros que podem ocorrer em múltiplos campos durante a validação do corpo da requisição.
}
