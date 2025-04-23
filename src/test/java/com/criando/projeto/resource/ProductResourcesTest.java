package com.criando.projeto.resource;

import com.criando.projeto.entities.Category;
import com.criando.projeto.entities.Product;
import com.criando.projeto.queryFIlters.ProductQueryFilter;
import com.criando.projeto.services.CategoryServices;
import com.criando.projeto.services.ProductServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ProductResourcesTest {
    @InjectMocks
    private ProductResources productResources;
    @Mock
    private ProductServices productServices;
    @Mock
    private ProductQueryFilter productQueryFilter;
    @Mock
    private Specification<Product> specification;

    private Product product;
    private Product productDois;

    @BeforeEach
    void setUp() {
        this.product = new Product();
        this.product.setId(1L);
        this.product.setName("Notebook");
        this.product.setDescription("Notebook para trabalho e estudos (foco na portabilidade e produtividade)");
        this.product.setPrice(5000.0);

        this.productDois = new Product();
        this.productDois.setId(2L);
        this.productDois.setName("Tablet");
        this.productDois.setDescription("Tablet para trabalho e estudos (foco na portabilidade e produtividade)");
        this.productDois.setPrice(3000.0);
    }

    @Test
    @DisplayName("Deve retornar uma lista de produtos com status 200 OK")
    void findAll() {
        List<Product> ProductList = List.of(product, productDois);
        when(productQueryFilter.toSpecification()).thenReturn(specification);
        when(productServices.findAll(specification)).thenReturn(ProductList);
        ResponseEntity<List<Product>> response = productResources.findAll(productQueryFilter);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ProductList, response.getBody());
        verify(productServices).findAll(specification);
    }

    @Test
    @DisplayName("Deve retornar um produto pelo ID com status 200 OK")
    void findById() {
        Mockito.when(productServices.findById(1L)).thenReturn(product);
        ResponseEntity<Product> response = productResources.findById(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(product, response.getBody());
        verify(productServices).findById(1L);
    }

    @Test
    @DisplayName("Deve criar um novo produto com status 201 Created")
    void createProduct() {
        Long categoryId = 1L;

        Product productToCreate = new Product();
        productToCreate.setName("Sabão");
        productToCreate.setDescription("Produto para limpezas leves");
        productToCreate.setPrice(5.99);

        Product savedProduct = new Product();
        savedProduct.setId(10L);
        savedProduct.setName("Sabão");
        productToCreate.setDescription("Produto para limpezas leves");
        savedProduct.setPrice(5.99);

        Mockito.when(productServices.insert(productToCreate, categoryId)).thenReturn(savedProduct);

        Product result = productResources.createProduct(productToCreate, categoryId);

        assertNotNull(result);
        assertEquals(savedProduct.getId(), result.getId());
        assertEquals(savedProduct.getName(), result.getName());
        assertEquals(savedProduct.getPrice(), result.getPrice());

        verify(productServices).insert(productToCreate, categoryId);
    }


    @Test
    @DisplayName("Deve deletar um produto com sucesso e retornar status 204 No Content ")
    void Delete() {
        doNothing().when(productServices).delete(1L);
        ResponseEntity<Void> response = productResources.delete(1L);
        verify(productServices, times(1)).delete(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    //cade o update?
}