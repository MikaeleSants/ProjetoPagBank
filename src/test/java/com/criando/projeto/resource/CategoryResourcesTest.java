package com.criando.projeto.resource;

import com.criando.projeto.entities.Category;
import com.criando.projeto.entities.Coupon;
import com.criando.projeto.services.CategoryServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import java.net.URI;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;


@ExtendWith(MockitoExtension.class)
class CategoryResourcesTest {
    @InjectMocks
    private CategoryResources categoryResources;
    @Mock
    private CategoryServices categoryServices;

    private Category category;
    private Category categoryDois;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Alimentos");

        categoryDois = new Category();
        categoryDois.setId(2L);
        categoryDois.setName("Roupas");
    }

    @Test
    @DisplayName("Deve retornar todas as categorias, com status 200 OK")
    void findAll() {
        List<Category> categoria = List.of(category, categoryDois);
        when(categoryServices.findAll()).thenReturn(categoria);
        ResponseEntity<List<Category>> result = categoryResources.findAll();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(categoria, result.getBody());

        verify(categoryServices).findAll();
    }

    @Test
    @DisplayName("Deve retornar uma categoria por ID, com status 200 OK")
    void findById() {
        when(categoryServices.findById(1L)).thenReturn(category);
        ResponseEntity<Category> result = categoryResources.findById(1L);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(category, result.getBody());

        verify(categoryServices).findById(1L);
    }

    @Test
    @DisplayName("Deve criar uma nova categoria, com status 200 OK")
    void insert() {
        Category newCategory = new Category();
        newCategory.setName("Limpeza");

        Category savedCategory = new Category();
        savedCategory.setId(3L);
        newCategory.setName("Limpeza");

        URI fakeUri = URI.create("/categories/3");

        // mock estático do builder
        try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = Mockito.mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = Mockito.mock(ServletUriComponentsBuilder.class);
            mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentRequest).thenReturn(builder);
            Mockito.when(builder.path("/{id}")).thenReturn(builder);
            Mockito.when(builder.buildAndExpand(savedCategory.getId())).thenReturn(Mockito.mock(UriComponents.class));
            Mockito.when(builder.buildAndExpand(savedCategory.getId()).toUri()).thenReturn(fakeUri);

            Mockito.when(categoryServices.insert(newCategory)).thenReturn(savedCategory);

            ResponseEntity<Category> response = categoryResources.insert(newCategory);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(savedCategory, response.getBody());
            assertEquals(fakeUri, response.getHeaders().getLocation());
            verify(categoryServices).insert(newCategory);
        }
    }

    @Test
    @DisplayName("Deve deletar uma categoria com sucesso e retornar status 204 No Content")
    void Delete() {
        doNothing().when(categoryServices).delete(category.getId());
        ResponseEntity<Void> response = categoryResources.delete(category.getId());
        verify(categoryServices, times(1)).delete(category.getId());
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve atualizar o cupom com sucesso e retornar o cupom atualizado")
    void Update() {
        Category updateCategory = new Category();
        updateCategory.setId(1L);
        updateCategory.setName("Limpeza");
        Mockito.when(categoryServices.update(category.getId(), category)).thenReturn(updateCategory);
        ResponseEntity<Category> response = categoryResources.update(category.getId(), category);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updateCategory, response.getBody());
        verify(categoryServices, times(1)).update(category.getId(), category);
    }

}
