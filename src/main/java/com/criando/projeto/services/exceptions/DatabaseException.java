package com.criando.projeto.services.exceptions;

public class DatabaseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DatabaseException() {
        super("Não é possível excluir o usuário pois há referências a ele em outras tabelas."); // Mensagem padrão
    }

    public DatabaseException(String message) {
        super(message);
    }
} //caso queira passar uma mensagem no codigo, algo como: throw new DatabaseException("Erro ao tentar excluir, pois o item está em uso.");

