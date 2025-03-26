package com.criando.projeto.resource;

import com.criando.projeto.entities.Product;
import com.criando.projeto.queryFIlters.ProductQueryFilter;
import com.criando.projeto.services.ProductServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/products")
public class ProductResources {
    @Autowired
    private ProductServices productServices;


    @GetMapping
    public ResponseEntity<List<Product>> findAll(ProductQueryFilter filter) {
        List<Product> list = productServices.findAll(filter.toSpecification());
        return ResponseEntity.ok().body(list);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Product> findById(@PathVariable Long id) {
        Product obj = productServices.findById(id);
        return ResponseEntity.ok().body(obj);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // Indica que o recurso foi criado com sucesso
    public Product createProduct(@RequestBody Product product, @RequestParam Long categoryId) {
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
    public ResponseEntity<Product> update(@PathVariable Long id, @Valid @RequestBody Product obj) {
        obj = productServices.update(id, obj);
        return ResponseEntity.ok().body(obj);
    }

    @PatchMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
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
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productServices.delete(id);
        return ResponseEntity.noContent().build();
    }

}
