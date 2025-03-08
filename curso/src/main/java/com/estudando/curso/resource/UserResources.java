package com.estudando.curso.resource;

import com.estudando.curso.entities.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/users")
public class UserResources {
    //responseEntity é um tipo do spring para retornar respostas de requisicoes web
    @GetMapping
    public ResponseEntity<User> findAll () {
        User u = new User(1L, "Maria", "maria@gmail.com", "99999999", "12345");
        //o primeiro atributo é do tipo Long, ai precisa por o L na frente
        return ResponseEntity.ok().body(u);
    }
}
