package com.criando.projeto.services;

import com.criando.projeto.entities.User;
import com.criando.projeto.repositories.UserRepository;
import com.criando.projeto.services.exceptions.AccessDeniedException;
import com.criando.projeto.services.exceptions.AuthenticationRequiredException;
import com.criando.projeto.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthenticationFacade {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    // Metodo para obter o e-mail do usuário autenticado
    public String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName(); // Retorna o e-mail do usuário logado
        }
        throw new AuthenticationRequiredException("Usuário não autenticado");
    }

    // Metodo para buscar o usuário autenticado
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = getAuthenticatedUserEmail(); // Obtém o e-mail do usuário autenticado

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + email ));
    }

    // Metodo para verificar se o usuário é ADMIN
    public boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }

    // Metodo para verificar se o usuário é USER
    public boolean isUser(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_USER"));
    }

    // Metodo para aplicar o id do usuário autenticado nas requisições
    public boolean isSameUser(Long userId) {
        try {
            User authenticatedUser = getAuthenticatedUser();
            return authenticatedUser.getId().equals(userId);
        } catch (Exception e) {
            throw new AccessDeniedException("Acesso negado! O ID logado não corresponde ao ID da busca");
        }
    }
}
