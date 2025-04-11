package com.criando.projeto.repositories;

import com.criando.projeto.entities.Category;
import com.criando.projeto.entities.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Product prod1;
    private Product prod2;
    private Category category;

    @BeforeEach
    void setup() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        category = new Category(null, "Eletrônicos");
        categoryRepository.save(category);

        prod1 = new Product(null, "Notebook", "Notebook potente", 3500.00);
        prod1.getCategories().add(category);

        prod2 = new Product(null, "Smartphone", "Celular moderno", 2500.00);
        prod2.getCategories().add(category);

        productRepository.saveAll(List.of(prod1, prod2));
    }

    @Test
    @DisplayName("Deve encontrar produto pelo ID")
    void deveEncontrarProdutoPorId() {
        Optional<Product> result = productRepository.findById(prod1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Notebook");
    }

    @Test
    @DisplayName("Deve retornar todos os produtos")
    void deveRetornarTodosProdutos() {
        List<Product> produtos = productRepository.findAll();

        assertThat(produtos).hasSize(2);
        assertThat(produtos).extracting(Product::getName)
                .containsExactlyInAnyOrder("Notebook", "Smartphone");
    }

    @Test
    @DisplayName("Deve salvar um novo produto")
    void deveSalvarNovoProduto() {
        Product novo = new Product(null, "Tablet", "Tablet intermediário", 1800.00);
        novo.getCategories().add(category);

        Product salvo = productRepository.save(novo);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getName()).isEqualTo("Tablet");
    }

    @Test
    @DisplayName("Deve atualizar um produto existente")
    void deveAtualizarProduto() {
        Product produto = productRepository.findById(prod1.getId()).orElseThrow();
        produto.setPrice(3700.00);

        Product atualizado = productRepository.save(produto);

        assertThat(atualizado.getPrice()).isEqualTo(3700.00);
    }

    @Test
    @DisplayName("Deve deletar um produto pelo ID")
    void deveDeletarProdutoPorId() {
        productRepository.deleteById(prod2.getId());

        Optional<Product> result = productRepository.findById(prod2.getId());
        assertThat(result).isEmpty();
    }
}
