package com.criando.projeto.resource;

import com.criando.projeto.entities.Coupon;
import com.criando.projeto.services.CouponServices;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Cupons", description = "Operações relacionadas aos cupons de desconto")
@RestController
@RequestMapping(value = "/cupons")
public class CouponResources {

    @Autowired
    private CouponServices couponServices;


    @GetMapping
    @Operation(summary = "Listar todos os cupons", description = "Retorna todos os cupons cadastrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de cupons retornada com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public ResponseEntity<List<Coupon>> findAll () {
        List<Coupon> list = couponServices.findAll();
        return ResponseEntity.ok().body(list); }

    @GetMapping(value = "/{id}")
    @Operation(summary = "Buscar cupom por ID", description = "Retorna os dados de um cupom específico a partir do seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cupom criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas administradores podem criar cupons"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public ResponseEntity<Coupon> findById(@PathVariable Long id) {
        Coupon obj = couponServices.findById(id);
        return ResponseEntity.ok().body(obj);
    }


    @PostMapping
    @Operation(summary = "Criar um novo cupom", description = "Adiciona um novo cupom ao sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cupom criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas administradores podem criar cupons"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public ResponseEntity<Coupon> insert(@Valid @RequestBody Coupon obj) {
        obj = couponServices.insert(obj);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getId()).toUri();
        return ResponseEntity.created(uri).body(obj);
    }

    @DeleteMapping(value = "/{id}")
    @Operation(summary = "Deletar cupom", description = "Remove um cupom do sistema pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cupom criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas administradores podem criar cupons"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        couponServices.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}")
    @Operation(summary = "Atualizar cupom", description = "Atualiza as informações de um cupom existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cupom criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas administradores podem criar cupons"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public ResponseEntity<Coupon> update(@PathVariable Long id, @Valid @RequestBody Coupon obj) {
        obj = couponServices.update(id, obj);
        return ResponseEntity.ok().body(obj);
    }
}