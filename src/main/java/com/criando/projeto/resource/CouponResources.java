package com.criando.projeto.resource;

import com.criando.projeto.entities.Coupon;
import com.criando.projeto.services.CouponServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/cupons")
public class CouponResources {

    @Autowired
    private CouponServices couponServices;


    @GetMapping
    public ResponseEntity<List<Coupon>> findAll () {
        List<Coupon> list = couponServices.findAll();
        return ResponseEntity.ok().body(list); }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Coupon> findById(@PathVariable Long id) {
        Coupon obj = couponServices.findById(id);
        return ResponseEntity.ok().body(obj);
    }
    /*O @PathVariable é uma anotação do Spring que indica que o valor do parâmetro do metodo deve ser extraído da parte da URL correspondente ao nome do parâmetro.
     @PathVariable Long id significa que o valor do id será extraído da URL quando a requisição for feita.
     */

    @PostMapping
    public ResponseEntity<Coupon> insert(@Valid @RequestBody Coupon obj) {
        obj = couponServices.insert(obj);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(obj.getId()).toUri();
        return ResponseEntity.created(uri).body(obj);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        couponServices.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<Coupon> update(@PathVariable Long id, @Valid @RequestBody Coupon obj) {
        obj = couponServices.update(id, obj);
        return ResponseEntity.ok().body(obj);
    }
}
