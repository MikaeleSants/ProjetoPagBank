package com.criando.projeto.services;

import com.criando.projeto.entities.User;
import com.criando.projeto.repositories.UserRepository;
import com.criando.projeto.services.exceptions.DatabaseException;
import com.criando.projeto.services.exceptions.EmailAlreadyExistsException;
import com.criando.projeto.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
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

        return userRepository.save(obj);
    }


    public void delete(Long id) {
        try {
        userRepository.deleteById(id); }
        catch (EmptyResultDataAccessException e) {throw new ResourceNotFoundException(id);} //Você tentou excluir um usuário com um id que não foi encontrado.
        catch (DataIntegrityViolationException e) {throw new DatabaseException(e.getMessage());} // Essa exceção ocorre quando a tentativa de exclusão viola uma restrição de integridade no banco de dados. Isso pode acontecer, por exemplo, quando um registro está sendo referenciado por outro
    }

    public User update(Long id, User obj) {
        try {
            User entity = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(id));
            updateData(entity, obj);

            if (obj.getEmail() != null) {
                Optional<User> existingUser = userRepository.findByEmail(obj.getEmail());
                if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                    throw new EmailAlreadyExistsException(obj.getEmail());
                }
            }

            return userRepository.save(entity);
        } catch (ResourceNotFoundException e) {
            throw e;
        }
    }


    private void updateData(User entity, User obj) {
        entity.setName(obj.getName());
        entity.setEmail(obj.getEmail());
        entity.setPhone(obj.getPhone());
        entity.setPassword(obj.getPassword());
    }

    public User updatePatch(Long id, User obj) {
        try {
            User entity = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(id));
            patchUpdateData(entity, obj);

            if (obj.getEmail() != null) {
                Optional<User> existingUser = userRepository.findByEmail(obj.getEmail());
                if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                    throw new EmailAlreadyExistsException(obj.getEmail());
                }
            }

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
            entity.setEmail(obj.getEmail());
        }
        if (obj.getPhone() != null) {
            entity.setPhone(obj.getPhone());
        }

        if (obj.getPassword() != null) {
            entity.setPassword(obj.getPassword());
        }
    }

}
