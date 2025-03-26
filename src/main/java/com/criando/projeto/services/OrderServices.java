package com.criando.projeto.services;

import com.criando.projeto.entities.*;
import com.criando.projeto.entities.enums.OrderStatus;
import com.criando.projeto.repositories.*;
import com.criando.projeto.services.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


//@Component registra a classe como componente, para que ela possa ser injetado automaticamente com o AutoWired
// Mas tem uma anotação que faz a mesma coisa, só que é semanticamente mais correta:
@Service
public class OrderServices {

    //injeta a Orderrepository, mas não precisamos botar o @Component na classe OrderRepository
    //como fizemos nessa, pq o OrderRepository extends JpaRepository, que já é marcado como componente
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;


// O metodo recebe um parâmetro Specification<Order>, que representa um critério de filtro para a busca.
//Ele chama orderRepository.findAll(specification), que delega a busca ao repositório.
//O metodo retorna uma lista de Order que correspondem aos filtros especificados.
    public List<Order> findOrders(Specification<Order> specification) {
        return orderRepository.findAll(specification); // Chama o findAll passando a Specification
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }


    public Order insert(Order order) {
        // Guarda os itens enviados na requisição,O var no Java é um tipo inferido introduzido no Java 10. Ele permite que o compilador determine automaticamente o tipo da variável com base no valor atribuído.
        var orderItems = order.getItems();

        // Zera os itens do pedido para evitar problemas de persistência, Quando você "zera" os itens do pedido com order.setItems(new HashSet<>()), a referência antiga para os itens é substituída, mas os objetos anteriores ainda existem na memória até que o Garbage Collector (GC) do Java os remova, caso não estejam mais sendo referenciados.
        order.setItems(new HashSet<>());

        // Se o pedido tiver um cupom, buscar no banco de dados
        Coupon coupon = applyCouponToOrder(order);
        if (coupon != null) {
            order.setDiscount(coupon);
        }

        // Salva o pedido sem itens
        var savedOrder = orderRepository.save(order);

        // Processa cada item para buscar o produto e definir corretamente o preço
        orderItems.forEach(orderItem -> {
            // Buscar o produto no banco de dados
            Product product = productRepository.findById(orderItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado: ID " + orderItem.getProduct().getId()));

            // Atualizar os dados do OrderItem com os dados do produto encontrado, de acordo com os atributos do order item
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setPrice(product.getPrice()); // Define o preço do produto no pedido
        });

        // Salva todos os itens com as informações do produto
        var savedItems = orderItemRepository.saveAll(orderItems);

        // Associa os itens ao pedido
        savedOrder.setItems(new HashSet<>(savedItems));

        return savedOrder;
    }


    public Order setOrderPayment(Long orderId, Payment payment) {
        // Buscar o pedido pelo ID
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        //verifica se o status é = a PAID ou CANCELED
        validateOrderStatus(order);

        // SE NÃO, atualiza os dados do pagamento e associa ao pedido
        payment.setOrder(order); //Order tem uma referência para Payment.
        order.setPayment(payment); //Payment tem uma referência para Order.

        // Verifica se o pagamento está preenchido e, se sim, altera o status do pedido para "PAID"
        if (payment.getPaymentMethod() != null) {
            // Considerando que o pagamento foi concluído se tiver valor e data de pagamento
            order.setOrderStatus(OrderStatus.PAID); // Ou outro status como "COMPLETED"
        }
        // Salva o pedido com os dados atualizados
        return orderRepository.save(order);
    }


    public Order setOrDeleteCoupon(Long orderId, Long couponId) {
        // Buscar o pedido pelo ID
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        validateOrderStatus(order);
        // Se o couponId não for nulo, buscar o cupom no banco de dados
        if (couponId != null) {
            Coupon coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new CouponNotFoundException("Cupom não encontrado: ID " + couponId));
            // Verificar se o cupom já foi aplicado, caso contrário, aplicar o novo cupom
            if (order.getDiscount() != null && order.getDiscount().getId().equals(couponId)) {
                throw new CouponAlreadyAppliedException(couponId);
            }
            order.setDiscount(coupon); // Associar o cupom ao pedido
        } else {
            // Se couponId for nulo, remover o cupom
            order.setDiscount(null);
        }
        // Salvar o pedido com as alterações
        return orderRepository.save(order);
    }


    public Order update(Long id, Order obj) {
        try {
            // Buscar o pedido pelo ID
            Order entity = orderRepository.findById(id)
                    .orElseThrow(() -> new OrderNotFoundException(id));
            // Fazendo a validação do status para confirmar se pode mudar
            validateOrderStatus(entity);
            // Atualizar os dados gerais do pedido
            updateData(entity, obj);
            // Salvar o pedido com as atualizações
            return orderRepository.save(entity);
        } catch (ResourceNotFoundException e) {
            throw e; // Garante que o erro 404 seja propagado corretamente
        }
    }
    private void updateData(Order entity, Order obj) {
        // Atualiza o status do pedido
        if (obj.getOrderStatus() != null) {
            entity.setOrderStatus(obj.getOrderStatus());
        }
        // Atualiza o cliente do pedido, buscando no banco se necessário
        if (obj.getClient() != null && obj.getClient().getId() != null) {
            User client = userRepository.findById(obj.getClient().getId())
                    .orElseThrow(() -> new UserNotFoundException("Cliente não encontrado: ID " + obj.getClient().getId()));
            entity.setClient(client);
        }
        if (obj.getDiscount() != null && obj.getDiscount().getId() != null) {
            Coupon coupon = couponRepository.findById(obj.getDiscount().getId())
                    .orElseThrow(() -> new CouponNotFoundException("Cupom não encontrado: ID " + obj.getDiscount().getId()));
            entity.setDiscount(coupon); // Atualiza o cupom no pedido
        } else {
            entity.setDiscount(null); // Se não passar um cupom, limpa o cupom
        }
    }


    // Atualizar ou adicionar itens ao pedido
    public Order updateOrderItems(Long orderId, Set<OrderItem> newItems) {
        // Buscar o pedido pelo ID
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        validateOrderStatus(order);
        // Atualizar ou adicionar os itens ao pedido
        updateItemsInOrder(order, newItems);

        // Salvar o pedido com os itens atualizados ou adicionados
        return orderRepository.save(order);
    }
    // Metodo responsável por atualizar ou adicionar itens ao pedido
    private void updateItemsInOrder(Order order, Set<OrderItem> newItems) {
        // Mapeia os itens existentes do pedido para um Map, usando o ID do produto como chave
        Map<Long, OrderItem> existingItemsMap = order.getItems().stream()
                .collect(Collectors.toMap(item -> item.getProduct().getId(), item -> item));
    /*order.getItems():
    Esse metodo retorna um conjunto (Set<OrderItem>) com todos os itens associados ao pedido (Order).
    .stream():
    Converte o Set<OrderItem> em um stream para poder aplicar operações de transformação (como collect) aos elementos. O stream permite fazer operações funcionais, como filtragem, mapeamento e coleta de dados.
    .collect(Collectors.toMap(...)):
    O collect é uma operação terminal que transforma o stream em uma coleção. Neste caso, estamos usando o Collectors.toMap() para coletar os itens em um mapa (Map).
    Collectors.toMap(item -> item.getProduct().getId(), item -> item):
    O toMap espera dois argumentos:
    Chave: O primeiro argumento é a função que define a chave do mapa. No caso, item.getProduct().getId() pega o ID do produto associado ao OrderItem e usa como chave do mapa.
    Valor: O segundo argumento é a função que define o valor do mapa. Aqui, item -> item retorna o próprio OrderItem, ou seja, o valor será o próprio item do pedido.
    Com isso, você cria um Map<Long, OrderItem>, onde a chave é o ID do produto e o valor é o próprio OrderItem.
    */
        //for (OrderItem newItem : newItems) {
        for (OrderItem newItem : newItems) {
            // Buscar ou atualizar item:
            //Aqui, o código usa o ID do produto do newItem para buscar um possível item existente no mapa existingItemsMap,
            //Se o item não existir no pedido, o get() retorna null
            OrderItem existingItem = existingItemsMap.get(newItem.getProduct().getId());
            //O código verifica se o item já existe no mapa (existingItemsMap). Se o existingItem for não nulo,
            // isso significa que o item com o mesmo produto já está presente no pedido.
            if (existingItem != null) {
                // Atualiza a quantidade do item existente e mantém o preço original
                existingItem.setQuantity(existingItem.getQuantity() + newItem.getQuantity());
            } else {
                // Caso o item não exista, associamos o produto e adicionamos ao pedido
                addNewItemToOrder(order, newItem);
            }
        }
    }
    // Metodo auxiliar para adicionar um novo item ao pedido
    private void addNewItemToOrder(Order order, OrderItem newItem) {
        // Busca o produto no banco para garantir que estamos associando um produto válido
        Product product = productRepository.findById(newItem.getProduct().getId())
                .orElseThrow(() -> new ProductNotFoundException("Produto não encontrado: ID " + newItem.getProduct().getId()));

        // Define as informações do novo item
        newItem.setOrder(order);         // Associa o item ao pedido
        newItem.setProduct(product);     // Associa o produto ao item
        newItem.setPrice(product.getPrice());  // Define o preço do produto no pedido

        // Adiciona o item à lista de itens do pedido
        order.getItems().add(newItem);
    }


    public Order updateOrderStatus(Long id, OrderStatus status) {
        Order entity = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
        validateOrderStatus(entity);

        entity.setOrderStatus(status);
        return orderRepository.save(entity);
    }

    public Order removeProductFromOrder(Long orderId, Long productId) {
        // Buscar o pedido pelo ID
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        validateOrderStatus(order);

        // Buscar o item do pedido que possui o produto com o ID fornecido
        OrderItem itemToRemove = order.getItems().stream()
                //order.getItems() retorna o conjunto de itens (Set<OrderItem>) associados ao pedido (Order).
                //.stream() converte esse conjunto (Set) em um stream, o que permite aplicar operações funcionais sobre ele, como filtragem e mapeamento.
                .filter(item -> item.getProduct().getId().equals(productId))
                //.filter(...) aplica um filtro a cada elemento do stream. Neste caso, ele filtra os itens de acordo com o ID do produto.
                //item.getProduct().getId() acessa o produto do item (OrderItem) e pega o ID desse produto.
                //.equals(productId) compara o ID do produto de cada item com o productId fornecido como parâmetro (ou seja, o ID do produto que você está buscando no pedido).
                //Esse filtro mantém apenas o item (produto) que tem o mesmo ID do produto fornecido.
                .findFirst()
                //.findFirst() retorna o primeiro elemento que corresponde ao filtro aplicado. Como o stream é filtrado para buscar um item específico (pelo ID do produto), o método findFirst irá retornar o primeiro item que corresponde ao filtro, ou seja, o item encontrado ou Optional.empty() caso o item não seja encontrado.
                .orElseThrow(() -> new ProductNotFoundException("Produto não encontrado no pedido"));

        // Remover o item do pedido
        order.getItems().remove(itemToRemove);
        // Salvar o pedido atualizado
        return orderRepository.save(order);
    }

    public void delete(Long id) {

        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new OrderNotFoundException(id));

            // Remove a referência ao pagamento (se existir)
            if (order.getPayment() != null) {
                order.setPayment(null);
            }

            // Remove a referência ao cupom (se existir)
            order.setDiscount(null);

            // Limpa os itens do pedido
            order.getItems().clear();
            orderRepository.save(order); // Salva a ordem sem itens antes de excluir

            orderRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    private void validateOrderStatus(Order order) {
        if (order.getOrderStatus() == OrderStatus.PAID || order.getOrderStatus() == OrderStatus.CANCELED) {
            throw new OrderStatusConflictException("Não é possível atualizar o pedido com status 'PAID' ou 'CANCELED'");
        }
    }

    private Coupon applyCouponToOrder(Order order) {
        if (order.getDiscount() != null && order.getDiscount().getId() != null) {
            return couponRepository.findById(order.getDiscount().getId())
                    .orElseThrow(() -> new CouponNotFoundException("Cupom não encontrado: ID " + order.getDiscount().getId()));
        }
        return null;
    }


}
