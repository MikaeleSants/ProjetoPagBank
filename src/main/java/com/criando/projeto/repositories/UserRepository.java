package com.criando.projeto.repositories;

import com.criando.projeto.entities.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    // Atualiza as senhas de todos os usuários
    //metodo foi criado para adaptar as senhas que foram construídas no banco, antes da lógica de autenticação ser implementada
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.password = :password")
    void updateAllPasswords(@Param("password") String password);
}
