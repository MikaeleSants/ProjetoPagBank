package com.criando.projeto.resource;

import com.criando.projeto.entities.Product;
import com.criando.projeto.entities.User;
import com.criando.projeto.entities.enums.UserRole;
import com.criando.projeto.services.AuthenticationFacade;
import com.criando.projeto.services.UserServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/users")
public class UserResources {
    @Autowired
    private UserServices service;
    @Autowired
    private AuthenticationFacade authenticationFacade;

    //responseEntity é um tipo do spring para retornar respostas de requisicoes web
    @GetMapping
    public ResponseEntity<List<User>> findAll () {
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

    @PostMapping
    public ResponseEntity<User> insert(@Valid @RequestBody User user, @AuthenticationPrincipal UserDetails userDetails) {

        // Verificar se o usuário é admin, caso contrário, define como "USER"
        if (userDetails != null && userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
            // Se for admin, o usuário pode ser criado com role "ADMIN"
            user.setRole(UserRole.ADMIN);  // Setar role ADMIN
        } else {
            // Senão, define como "USER"
            user.setRole(UserRole.USER);
        }

        user = service.insert(user);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(user.getId()).toUri();
    return ResponseEntity.created(uri).body(user);}

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<User> updatePatch(@PathVariable Long id, @RequestBody User obj, Authentication authentication) {
        User updatedUser = service.updatePatch(id, obj, authentication);
        return ResponseEntity.ok(updatedUser);
    }


//    @PostMapping("/update-passwords")
//    public ResponseEntity<Void> updatePasswords() {
//        service.updatePasswordsForAllUsers();  // Chama o metodo para atualizar todas as senhas
//        return ResponseEntity.noContent().build();  // Retorna uma resposta 204 sem conteúdo
//    }
}
