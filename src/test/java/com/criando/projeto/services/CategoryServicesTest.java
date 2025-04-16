package com.criando.projeto.services;

import com.criando.projeto.entities.Category;
import com.criando.projeto.repositories.CategoryRepository;
import com.criando.projeto.services.exceptions.DatabaseException;
import com.criando.projeto.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServicesTest {

    @InjectMocks
    private CategoryServices categoryServices;

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Deve retornar todas as categorias")
    void findAll() {
        Category c1 = new Category(1L, "Eletrônicos");
        Category c2 = new Category(2L, "Roupas");
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(c1, c2));

        List<Category> result = categoryServices.findAll();

        assertThat(result).hasSize(2).contains(c1, c2);
    }

    @Test
    @DisplayName("Deve retornar categoria por ID")
    void findById() {
        Category category = new Category(1L, "Alimentos");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Category result = categoryServices.findById(1L);

        assertThat(result).isEqualTo(category);
    }

    @Test
    @DisplayName("Deve lançar exceção se ID não existir")
    void findByIdException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryServices.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Categoria não encontrada");
    }

    @Test
    @DisplayName("Deve inserir nova categoria")
    void insert() {
        Category category = new Category(null, "Livros");
        Category saved = new Category(1L, "Livros");
        when(categoryRepository.save(category)).thenReturn(saved);

        Category result = categoryServices.insert(category);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Livros");
    }

    @Test
    @DisplayName("Deve deletar categoria existente")
    void delete() {
        categoryServices.delete(1L);
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar ID inexistente")
    void deleteExceptionNotFound() {
        doThrow(new EmptyResultDataAccessException(1)).when(categoryRepository).deleteById(99L);

        assertThatThrownBy(() -> categoryServices.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção de integridade ao deletar")
    void deleteDatabaseException() {
        doThrow(new DataIntegrityViolationException("Integridade violada")).when(categoryRepository).deleteById(1L);

        assertThatThrownBy(() -> categoryServices.delete(1L))
                .isInstanceOf(DatabaseException.class);
    }

    @Test
    @DisplayName("Deve atualizar categoria existente")
    void update() {
        Long id = 1L;
        Category existente = new Category(id, "Velho");
        Category atualizado = new Category(id, "Novo");

        when(categoryRepository.findById(id)).thenReturn(Optional.of(existente));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category result = categoryServices.update(id, atualizado);

        assertThat(result.getName()).isEqualTo("Novo");
        verify(categoryRepository).save(existente);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar categoria inexistente")
    void updateException() {
        Long id = 99L;
        Category atualizado = new Category(id, "Teste");

        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryServices.update(id, atualizado))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}