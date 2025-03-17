package com.criando.projeto.resource;

import com.criando.projeto.entities.Order;
import com.criando.projeto.queryFIlters.OrderQueryFilter;
import com.criando.projeto.services.OrderServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/orders")
public class OrderResources {
    @Autowired
    private OrderServices orderServices;

    //responseEntity é um tipo do spring para retornar respostas de requisicoes web
    @GetMapping
    public ResponseEntity<List<Order>> findAll(OrderQueryFilter filter) {
        List<Order> list = orderServices.findAll(filter.toSpecification());
        return ResponseEntity.ok().body(list);
    }

    //aqui eu vou botar na url o valor do id do usuario pra buscar, pra dizer que a minha url
    //recebe um paramentro, eu uso o que tem em ({}), em seguida botar uma annotation @PathVariable
    //ao lado da variavel do paramentro do metodo
    @GetMapping(value = "/{id}")
    public ResponseEntity<Order> findById(@PathVariable Long id) {
        Order obj = orderServices.findById(id);
        return ResponseEntity.ok().body(obj);
    }
}
