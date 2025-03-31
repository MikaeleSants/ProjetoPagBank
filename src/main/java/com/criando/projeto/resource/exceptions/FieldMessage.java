package com.criando.projeto.resource.exceptions;

import java.io.Serializable;

public class FieldMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fieldName;
    private String message;

    public FieldMessage(String fieldName, String message) {
        this.fieldName = fieldName;
        this.message = message;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    //armazenar os detalhes de um erro específico de um campo. Não vejo necessidade de mudanças aqui, já que ela está mantendo o nome do campo e a mensagem de erro, que são essenciais para erros de validação.
}