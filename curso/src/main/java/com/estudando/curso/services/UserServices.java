package com.estudando.curso.services;

import com.estudando.curso.entities.User;
import com.estudando.curso.repositories.UserRepository;
import org.hibernate.annotations.Comment;
import org.springframework.beans.factory.annotation.Autowired;
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
        return obj.get();
    }
}
