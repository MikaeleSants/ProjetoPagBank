package com.criando.projeto.repositories;

import com.criando.projeto.entities.User;
import com.criando.projeto.entities.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        user1 = new User(null, "Maria Silva", "maria@test.com", "85999999999", "123456", UserRole.USER);
        user2 = new User(null, "João Souza", "joao@test.com", "85988888888", "abcdef", UserRole.ADMIN);

        userRepository.saveAll(List.of(user1, user2));
    }

    @Test
    @DisplayName("Deve buscar usuário por ID")
    void deveBuscarUsuarioPorId() {
        Optional<User> result = userRepository.findById(user1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Maria Silva");
    }

    @Test
    @DisplayName("Deve encontrar usuário pelo e-mail")
    void deveEncontrarPorEmail() {
        Optional<User> result = userRepository.findByEmail("maria@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Maria Silva");
    }

    @Test
    @DisplayName("Deve retornar todos os usuários")
    void deveRetornarTodosUsuarios() {
        List<User> users = userRepository.findAll();

        assertThat(users).hasSize(2);
        assertThat(users)
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder("maria@test.com", "joao@test.com");
    }

    @Test
    @DisplayName("Deve salvar um novo usuário")
    void deveSalvarUsuario() {
        User novo = new User(null, "Ana Lima", "ana@test.com", "85977777777", "senha123", UserRole.USER);
        User salvo = userRepository.save(novo);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getEmail()).isEqualTo("ana@test.com");
    }

    @Test
    @DisplayName("Deve atualizar o nome e o telefone do usuário")
    void deveAtualizarUsuario() {
        User user = userRepository.findById(user1.getId()).orElseThrow();
        user.setName("Maria Oliveira");
        user.setPhone("85900000000");
        User atualizado = userRepository.save(user);

        assertThat(atualizado.getName()).isEqualTo("Maria Oliveira");
        assertThat(atualizado.getPhone()).isEqualTo("85900000000");
    }

    @Test
    @DisplayName("Deve deletar um usuário pelo ID")
    void deveDeletarUsuario() {
        userRepository.deleteById(user2.getId());

        Optional<User> result = userRepository.findById(user2.getId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve lançar exceção ao salvar e-mail duplicado")
    void deveLancarExcecaoParaEmailDuplicado() {
        User duplicado = new User(null, "Outra Maria", "maria@test.com", "85999911111", "senha999", UserRole.USER);

        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicado))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção se campos obrigatórios forem nulos")
    void deveLancarExcecaoParaCamposInvalidos() {
        User invalido = new User();
        assertThatThrownBy(() -> userRepository.saveAndFlush(invalido))
                .isInstanceOf(ConstraintViolationException.class);
    }
}
