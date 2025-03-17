package com.criando.projeto.services;

import com.criando.projeto.entities.Order;
import com.criando.projeto.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;


//@Component registra a classe como componente, para que ela possa ser injetado automaticamente com o AutoWired
// Mas tem uma anotação que faz a mesma coisa, só que é semanticamente mais correta:
@Service
public class OrderServices {

    //injeta a Orderrepository, mas não precisamos botar o @Component na classe OrderRepository
    //como fizemos nessa, pq o OrderRepository extends JpaRepository, que já é marcado como componente
    @Autowired
    private OrderRepository orderRepository;

    public List<Order> findAll(Specification<Order> spec) {
        return orderRepository.findAll(spec);}

    public Order findById(Long id) {
        Optional <Order> obj =  orderRepository.findById(id);
        return obj.get();
    }
}
