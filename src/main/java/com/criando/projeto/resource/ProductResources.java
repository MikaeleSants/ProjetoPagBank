package com.criando.projeto.resource;

import com.criando.projeto.entities.Product;
import com.criando.projeto.queryFIlters.ProductQueryFilter;
import com.criando.projeto.resource.exceptions.ValidationError;
import com.criando.projeto.services.ProductServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Produtos", description = "Endpoints para gerenciamento de produtos")
@RestController
@RequestMapping(value = "/products")
public class ProductResources {
    @Autowired
    private ProductServices productServices;


    @GetMapping
    @Operation(
            summary = "Listar todos os produtos", description = "Retorna uma lista com todos os produtos cadastrados. É possível aplicar filtros dinâmicos usando parâmetros."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public ResponseEntity<List<Product>> findAll(ProductQueryFilter filter) {
        List<Product> list = productServices.findAll(filter.toSpecification());
        return ResponseEntity.ok().body(list);
    }

    @GetMapping(value = "/{id}")
    @Operation(
            summary = "Buscar produto por ID", description = "Retorna os detalhes de um produto específico com base no ID fornecido."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto encontrado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public ResponseEntity<Product> findById(@PathVariable Long id) {
        Product obj = productServices.findById(id);
        return ResponseEntity.ok().body(obj);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar um novo produto", description = "Cria um novo produto no sistema e associa a uma categoria existente através do parâmetro categoryId."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produto criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "422", description = "Erro de validação nos dados enviados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationError.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso proibido"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public Product createProduct(@Valid @RequestBody Product product, @RequestParam Long categoryId) {
        return productServices.insert(product, categoryId);
    }
    /*
        POST /products?categoryId=1
        Content-Type: application/json

    {
        "name": "Produto A",
        "description": "Descrição do Produto A",
        "price": 199.99
    }
    */

    @PutMapping(value = "/{id}")
    @Operation(
            summary = "Atualizar um produto", description = "Atualiza todas as informações de um produto existente com base no ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "422", description = "Erro de validação nos dados enviados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationError.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso proibido"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public ResponseEntity<Product> update(@PathVariable Long id, @Valid @RequestBody Product obj) {
        obj = productServices.update(id, obj);
        return ResponseEntity.ok().body(obj);
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Atualizar parcialmente um produto", description = "Atualiza apenas os campos informados de um produto existente. Campos não enviados permanecerão inalterados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "422", description = "Erro de validação nos dados enviados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationError.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso proibido"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public Product updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        return productServices.updatePartial(id, product);
    }
    /*
     {
      "name": "Camiseta de Algodão",
      "description": "Camiseta confortável para o dia a dia",
      "price": 49.90,
      "categories": [
        { "id": 1, "name": "Roupas" },
        { "id": 2, "name": "Moda" }
      ]
    }
    */


    @DeleteMapping(value = "/{id}")
    @Operation(
            summary = "Deletar um produto", description = "Remove um produto do sistema com base no ID fornecido. Retorna erro 404 se o produto não for encontrado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produto deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "400", description = "Produto não pode ser deletado devido a vínculos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso proibido"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productServices.delete(id);
        return ResponseEntity.noContent().build();
    }

}
