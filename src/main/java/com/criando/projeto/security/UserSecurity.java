package com.criando.projeto.security;
import com.criando.projeto.entities.User;
import com.criando.projeto.repositories.UserRepository;
import com.criando.projeto.services.exceptions.AuthenticationRequiredException;
import com.criando.projeto.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class UserSecurity {

    @Autowired
    private UserRepository userRepository;

    public boolean checkUserOwnership(Authentication authentication, Long userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationRequiredException("Acesso negado: usuário não autenticado");
        }

        // Obtém o e-mail do usuário autenticado
        String email = authentication.getName();

        // Busca o usuário pelo e-mail
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Se o usuário for ADMIN, permite o acesso
        if (user.getRole() != null && "ADMIN".equals(user.getRole().name())) {
            return true;
        }

        // Permite apenas se o ID do usuário autenticado for igual ao ID da requisição
        return user.getId().equals(userId);
    }
}

