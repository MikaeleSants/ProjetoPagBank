package com.criando.projeto.services;

import com.criando.projeto.entities.Category;
import com.criando.projeto.entities.Product;
import com.criando.projeto.repositories.CategoryRepository;
import com.criando.projeto.repositories.ProductRepository;
import com.criando.projeto.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServicesTest {

    @InjectMocks
    private ProductServices productServices;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Deve retornar produto por ID")
    void findById() {
        Product product = new Product(1L, "Produto A", "Descrição A", 100.0);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productServices.findById(1L);

        assertThat(result).isEqualTo(product);
    }

    @Test
    @DisplayName("Deve lançar exceção se produto não for encontrado")
    void findByIdException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productServices.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Produto não encontrado");
    }

    @Test
    @DisplayName("Deve inserir produto com categoria existente")
    void insert() {
        Long categoryId = 1L;
        Category category = new Category(categoryId, "Eletrônicos");
        Product product = new Product(null, "Novo Produto", "Desc", 99.9);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productServices.insert(product, categoryId);

        assertThat(result.getCategories()).contains(category);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Deve lançar exceção se categoria não for encontrada no insert")
    void insertExceptionCategoria() {
        when(categoryRepository.findById(42L)).thenReturn(Optional.empty());

        Product novo = new Product(null, "Novo", "Desc", 50.0);

        assertThatThrownBy(() -> productServices.insert(novo, 42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Categoria não encontrada");
    }

    @Test
    @DisplayName("Deve atualizar um produto existente")
    void update() {
        Long id = 1L;
        Product existente = new Product(id, "Antigo", "Desc antiga", 10.0);
        Product atualizado = new Product(id, "Novo", "Nova desc", 20.0);

        when(productRepository.findById(id)).thenReturn(Optional.of(existente));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productServices.update(id, atualizado);

        assertThat(result.getName()).isEqualTo("Novo");
        assertThat(result.getDescription()).isEqualTo("Nova desc");
        assertThat(result.getPrice()).isEqualTo(20.0);
        verify(productRepository).save(existente);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar produto inexistente")
    void updateException() {
        Long idInexistente = 99L;
        Product atualizado = new Product(idInexistente, "Novo", "Desc", 30.0);

        when(productRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productServices.update(idInexistente, atualizado))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve fazer atualização parcial de um produto")
    void updatePartial() {
        Long id = 1L;
        Product existente = new Product(id, "Velho", "Desc velha", 10.0);
        Product parcial = new Product();
        parcial.setPrice(99.0);

        when(productRepository.findById(id)).thenReturn(Optional.of(existente));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productServices.updatePartial(id, parcial);

        assertThat(result.getPrice()).isEqualTo(99.0);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar produto inexistente")
    void deleteException() {
        doThrow(new EmptyResultDataAccessException(1)).when(productRepository).deleteById(100L);

        assertThatThrownBy(() -> productServices.delete(100L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Produto não encontrado");
    }
}