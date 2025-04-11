package com.criando.projeto.repositories;

import com.criando.projeto.entities.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;


@DataJpaTest
@ActiveProfiles("test")
class CategoryRepositoryTest {

        @Autowired
        private CategoryRepository categoryRepository;

    private Category cat1;
    private Category cat2;
    private Category cat3;

    //criei as categorias antes e usei o BeforeEach para indicar que esse metodo precisa ser executado antes de cada teste, pra criar os dados novamente
        @BeforeEach
        void setup() {
            categoryRepository.deleteAll();

            cat1 = new Category(null, "Electronics");
            cat2 = new Category(null, "Books");
            cat3 = new Category(null, "Computers");

            categoryRepository.saveAll(List.of(cat1, cat2, cat3));
        }

        @Test
        @DisplayName("Deve encontrar categoria pelo ID")
        void deveEncontrarCategoriaPorId() {
            Optional<Category> result = categoryRepository.findById(cat1.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Electronics");
        }

        @Test
        @DisplayName("Deve retornar todas as categorias")
        void deveRetornarTodasCategorias() {
            var categorias = categoryRepository.findAll();

            assertThat(categorias).hasSize(3);
            assertThat(categorias).extracting(Category::getName)
                    .containsExactlyInAnyOrder("Electronics", "Books", "Computers");
        }

        @Test
        @DisplayName("Deve salvar uma nova categoria")
        void deveSalvarNovaCategoria() {
            Category nova = new Category(null, "Games");
            Category salva = categoryRepository.save(nova);

            assertThat(salva.getId()).isNotNull();
            assertThat(salva.getName()).isEqualTo("Games");
        }

        @Test
        @DisplayName("Deve atualizar uma categoria existente")
        void deveAtualizarCategoria() {
            Category categoria = categoryRepository.findById(cat1.getId()).orElseThrow();
            categoria.setName("Eletrônicos Atualizado");

            Category atualizada = categoryRepository.save(categoria);

            assertThat(atualizada.getName()).isEqualTo("Eletrônicos Atualizado");
        }

        @Test
        @DisplayName("Deve deletar uma categoria pelo ID")
        void deveDeletarCategoriaPorId() {
            categoryRepository.deleteById(cat3.getId());

            Optional<Category> result = categoryRepository.findById(cat3.getId());
            assertThat(result).isEmpty();
        }
}