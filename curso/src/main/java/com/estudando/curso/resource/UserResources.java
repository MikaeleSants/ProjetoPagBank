package com.estudando.curso.resource;

import com.estudando.curso.entities.User;
import com.estudando.curso.services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/users")
public class UserResources {
    @Autowired
    private UserServices service;

    //responseEntity é um tipo do spring para retornar respostas de requisicoes web
    @GetMapping
    public ResponseEntity<List<User>> findAll () {
        //User u = new User(1L, "Maria", "maria@gmail.com", "99999999", "12345");
        //o primeiro atributo é do tipo Long, ai precisa por o L na frente
        List<User> list = service.findAll();
        return ResponseEntity.ok().body(list);
    }

    //aqui eu vou botar na url o valor do id do usuario pra buscar, pra dizer que a minha url
    //recebe um paramentro, eu uso o que tem em ({}), em seguida botar uma annotation @PathVariable
    //ao lado da variavel do paramentro do metodo
    @GetMapping(value = "/{id}")
    public ResponseEntity<User> findById(@PathVariable Long id) {
        User obj = service.findById(id);
        return ResponseEntity.ok().body(obj);
    }
}
