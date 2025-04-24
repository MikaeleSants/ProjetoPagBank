package com.criando.projeto.resource;

import com.criando.projeto.entities.User;
import com.criando.projeto.entities.enums.UserRole;
import com.criando.projeto.resource.exceptions.ValidationError;
import com.criando.projeto.services.AuthenticationFacade;
import com.criando.projeto.services.UserServices;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;


@Tag(name = "Usuários", description = "Operações relacionadas aos usuários do sistema")
@RestController
@RequestMapping(value = "/users")
public class UserResources {
    @Autowired
    private UserServices service;
    @Autowired
    private AuthenticationFacade authenticationFacade;


    @GetMapping
    @Operation(summary = "Buscar todos os usuários", description = "Apenas admins podem visualizar todos. Usuários comuns só veem a si mesmos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuários encontrados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso proibido"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public ResponseEntity<List<User>> findAll () {
        List<User> list = service.findAll();
        return ResponseEntity.ok().body(list);
    }


    @GetMapping(value = "/{id}")
    @Operation(summary = "Buscar usuário por ID", description = "Admins podem buscar qualquer usuário. Usuários comuns apenas a si mesmos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso proibido"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public ResponseEntity<User> findById(@PathVariable Long id) {
        User obj = service.findById(id);
        return ResponseEntity.ok().body(obj);
    }

    @PostMapping
    @Operation(summary = "Criar novo usuário", description = "Admins podem criar usuários com qualquer role. Usuários comuns criam apenas com role USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos na requisição"),
            @ApiResponse(responseCode = "422", description = "Erro de validação nos dados enviados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationError.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
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
    @Operation(summary = "Deletar usuário", description = "Usuário pode deletar a si mesmo. Admin pode deletar qualquer usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso proibido"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor"),
            @ApiResponse(responseCode = "400", description = "Violação de integridade referencial")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar parcialmente o usuário", description = "Usuário pode atualizar a si mesmo. Admin pode atualizar qualquer usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição malformada"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "422", description = "Erro de validação nos dados enviados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationError.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public ResponseEntity<User> updatePatch(@PathVariable Long id, @RequestBody User obj, Authentication authentication) {
        User updatedUser = service.updatePatch(id, obj, authentication);
        return ResponseEntity.ok(updatedUser);
    }

}