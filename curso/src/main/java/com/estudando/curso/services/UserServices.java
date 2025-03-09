package com.estudando.curso.services;

import com.estudando.curso.entities.User;
import com.estudando.curso.repositories.UserRepository;
import com.estudando.curso.services.exceptions.DatabaseException;
import com.estudando.curso.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.annotations.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
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
