package com.criando.projeto.services;

import com.criando.projeto.entities.User;
import com.criando.projeto.repositories.UserRepository;
import com.criando.projeto.services.exceptions.DatabaseException;
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



    public List<User> findAll() {
        return userRepository.findAll();
    }
    public User findById(Long id) {
        Optional <User> obj =  userRepository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException(id));
    }
    public User insert(User obj) {
        return userRepository.save(obj);
    }
    public void delete(Long id) {
        try {
        userRepository.deleteById(id); } catch (EmptyResultDataAccessException e)
        {throw new ResourceNotFoundException(id);} catch (DataIntegrityViolationException e)
        {throw new DatabaseException(e.getMessage());}
    }
    //precisei atualizar esse trecho com a ajuda do chatgpt, porque o do curso estava desatualizado
    public User update(Long id, User obj) {
        try {
            User entity = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(id)); // Lança 404 se não encontrar
            updateData(entity, obj);
            return userRepository.save(entity);
        } catch (ResourceNotFoundException e) {
            throw e; // Garante que o erro 404 seja propagado corretamente
        }
    }

    private void updateData(User entity, User obj) {
        entity.setName(obj.getName());
        entity.setEmail(obj.getEmail());
        entity.setPhone(obj.getPhone());
    }
}
