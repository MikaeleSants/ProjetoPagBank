package com.criando.projeto.entities;

import com.criando.projeto.entities.enums.UserRole;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Deve validar com dados corretos")
    void deveValidarUsuarioValido() {
        User user = new User(null, "Fulano", "fulano@email.com", "11999998888", "Sen@123", UserRole.USER);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Usuário deveria ser válido");
    }

    @Test
    @DisplayName("Deve falhar com nome vazio")
    void deveFalharComNomeVazio() {
        User user = new User(null, "", "email@email.com", "11999998888", "senha", UserRole.USER);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    @DisplayName("Deve falhar com email inválido")
    void deveFalharComEmailInvalido() {
        User user = new User(null, "João", "emailInvalido", "11999998888", "senha", UserRole.USER);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("Deve falhar com telefone curto")
    void deveFalharComTelefoneCurto() {
        User user = new User(null, "João", "joao@email.com", "123", "senha", UserRole.USER);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phone")));
    }

    @Test
    @DisplayName("Deve falhar com senha vazia")
    void deveFalharComSenhaVazia() {
        User user = new User(null, "João", "joao@email.com", "11999998888", "", UserRole.USER);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }
}