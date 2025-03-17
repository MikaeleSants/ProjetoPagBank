package com.criando.projeto.resource;

import com.criando.projeto.entities.Product;
import com.criando.projeto.queryFIlters.ProductQueryFilter;
import com.criando.projeto.services.ProductServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<Product> insert(@Valid @RequestBody Product obj) {
        obj = productServices.insert(obj);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getId()).toUri();
        return ResponseEntity.created(uri).body(obj);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productServices.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @Valid @RequestBody Product obj) {
        obj = productServices.update(id, obj);
        return ResponseEntity.ok().body(obj);
    }
}
