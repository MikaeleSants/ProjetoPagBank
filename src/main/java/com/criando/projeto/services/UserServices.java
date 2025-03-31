package com.criando.projeto.services;

import com.criando.projeto.entities.User;
import com.criando.projeto.repositories.UserRepository;
import com.criando.projeto.services.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

//@Component registra a classe como componente, para que ela possa ser injetado automaticamente com o AutoWired
// Mas tem uma anotação que faz a mesma coisa, só que é semanticamente mais correta:
@Service
public class UserServices {

    //injeta a userrepository, mas não precisamos botar o @Component na classe UserRepository
    //como fizemos nessa, pq o UserRepository extends JpaRepository, que já é marcado como componente
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder; // Injetando o PasswordEncoder
    @Autowired
    private AuthenticationFacade authenticationFacade;


//    @Autowired
//    private BCryptPasswordEncoder passwordEncoderDois;
//
//    public void updatePasswordsForAllUsers() {
//        List<User> users = userRepository.findAll();
//
//        for (User user : users) {
//            if (user.getPassword() != null) {
//                // Codifica a senha com BCrypt
//                String encodedPassword = passwordEncoder.encode(user.getPassword());
//                user.setPassword(encodedPassword);
//                userRepository.save(user); // Atualiza a senha no banco
//            }
//        }
//    }


    // O metodo findAll(), findbyId vem do JpaRepository
    public List<User> findAll() {
        return userRepository.findAll();
    }
    public User findById(Long id) {
        Optional <User> obj =  userRepository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException(id));
    }
    public User insert(User obj) {
        Optional<User> existingUser = userRepository.findByEmail(obj.getEmail());
        if (existingUser.isPresent()) {
            throw new EmailAlreadyExistsException(obj.getEmail());
        }

        // Validação de senha
        validatePassword(obj.getPassword());

        // Codifica a senha com BCrypt
        obj.setPassword(passwordEncoder.encode(obj.getPassword()));

        return userRepository.save(obj);
    }


    public void delete(Long id) {
        try {
        userRepository.deleteById(id); }
        catch (EmptyResultDataAccessException e) {throw new ResourceNotFoundException(id);} //Você tentou excluir um usuário com um id que não foi encontrado.
        catch (DataIntegrityViolationException e) {throw new DatabaseException(e.getMessage());} // Essa exceção ocorre quando a tentativa de exclusão viola uma restrição de integridade no banco de dados. Isso pode acontecer, por exemplo, quando um registro está sendo referenciado por outro
    }
    //AQUI
    public User updatePatch(Long id, User obj) {
        try {
            User entity = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(id));
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
            throw new EmailAlreadyExistsException(newEmail);
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
