package com.criando.projeto.services;

import com.criando.projeto.entities.User;
import com.criando.projeto.repositories.UserRepository;
import com.criando.projeto.security.UserSecurity;
import com.criando.projeto.services.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserServices {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder; // Injetando o PasswordEncoder
    @Autowired
    private AuthenticationFacade authenticationFacade;
    @Autowired
    private UserSecurity userSecurity;






    public List<User> findAll() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Se o usuário for do role USER, ele só pode ver o próprio usuário
        if (authenticationFacade.isUser(authentication)) {
            // Define automaticamente o userId para o id do usuário logado
            Long userId = authenticationFacade.getAuthenticatedUser().getId();
            // Buscar o usuário específico usando o userId
            return userRepository.findById(userId).map(Collections::singletonList).orElse(Collections.emptyList());
        }
        // Se for ADMIN ou outro papel, pode retornar todos os usuários
        return userRepository.findAll();
    }


    public User findById(Long id) {
        Optional <User> obj =  userRepository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException(id));
    }


    public User insert(User obj) {
        Optional<User> existingUser = userRepository.findByEmail(obj.getEmail());
        if (existingUser.isPresent()) {
            throw new EmailAlreadyExistsException("O e-mail informado já está em uso.");
        }
        validatePassword(obj.getPassword());
        // Codifica a senha com BCrypt
        obj.setPassword(passwordEncoder.encode(obj.getPassword()));
        return userRepository.save(obj);
    }


    public void delete(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Verifica se o usuário tem permissão para deletar (próprio usuário ou admin)
        if (!userSecurity.checkUserOwnership(authentication, id)) {
            throw new AccessDeniedException("Você não tem permissão para excluir este usuário.");
        }
        try {
            userRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Não é possível excluir este usuário, pois há referências a ele.");
        }
    }



    public User updatePatch(Long id, User obj, Authentication authentication) {
        try {
            User entity = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(id));

            User authenticatedUser = authenticationFacade.getAuthenticatedUser(); // Obtém o usuário logado

            // Se o usuário autenticado NÃO for admin e tentar atualizar outro usuário, lançar erro
            if (!authenticationFacade.isAdmin(authentication) && !authenticatedUser.getId().equals(id)) {
                throw new AccessDeniedException("Você só pode atualizar os seus próprios dados.");
            }
            patchUpdateData(entity, obj);
            return userRepository.save(entity);
        } catch (ResourceNotFoundException e) {
            throw e;
        }
    }

    private void patchUpdateData(User entity, User obj) {
        if (obj.getName() != null) {
            entity.setName(obj.getName());
        }
        if (obj.getEmail() != null) {
            validateEmail(entity, obj.getEmail());  // Validação do email
            entity.setEmail(obj.getEmail());
        }
        if (obj.getPhone() != null) {
            entity.setPhone(obj.getPhone());
        }
        if (obj.getPassword() != null) {
            validatePassword(obj.getPassword());
            entity.setPassword(passwordEncoder.encode(obj.getPassword())); // Criptografando a senha, se for fornecida
        }
    }



// METODOS AUXILIARES:
    private void validateEmail(User entity, String newEmail) {
        Optional<User> existingUser = userRepository.findByEmail(newEmail);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(entity.getId())) {
            throw new EmailAlreadyExistsException("Email já está em uso");
        }
    }

    private void validatePassword(String password) {
        if (password.length() < 3 || password.length() > 8) {
            throw new InvalidPasswordLengthException();
        }
        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$")) {
            throw new InvalidPasswordPatternException();
        }
    }
}
