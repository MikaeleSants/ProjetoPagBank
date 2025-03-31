package com.criando.projeto.services;

import com.criando.projeto.entities.Category;
import com.criando.projeto.entities.Product;
import com.criando.projeto.repositories.CategoryRepository;
import com.criando.projeto.repositories.ProductRepository;
import com.criando.projeto.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServices {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    public List<Product> findAll(Specification<Product> spec) {
        return productRepository.findAll(spec);
    }

    public Product findById(Long id) {
        Optional <Product> obj =  productRepository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado. ID: " + id));
    }
    public Product insert(Product product, Long categoryId) {
        // Buscar a categoria pelo ID
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada. ID: " + categoryId));

        // Associa a categoria ao produto
        product.getCategories().add(category); // Adiciona a categoria ao conjunto de categorias do produto

        // Salva o produto com a categoria associada
        return productRepository.save(product);
    }


    //precisei atualizar esse trecho com a ajuda do chatgpt, porque o do curso estava desatualizado
    public Product update(Long id, Product obj) {
        try {
            Product entity = productRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado. ID: " + id)); // Lança 404 se não encontrar
            updateData(entity, obj);
            return productRepository.save(entity);
        } catch (ResourceNotFoundException e) {
            throw e; // Garante que o erro 404 seja propagado corretamente
        }
    }

    private void updateData(Product entity, Product obj) {
        entity.setName(obj.getName());
        entity.setDescription(obj.getDescription());
        entity.setPrice(obj.getPrice());
    }

    public Product updatePartial(Long id, Product newData) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: ID " + id));

        // Atualiza apenas os campos não nulos
        if (newData.getName() != null) {
            product.setName(newData.getName());
        }
        if (newData.getDescription() != null) {
            product.setDescription(newData.getDescription());
        }
        if (newData.getPrice() != null) {
            product.setPrice(newData.getPrice());
        }
        if (newData.getCategories() != null && !newData.getCategories().isEmpty()) {
            //O primeiro != null garante que o objeto newData.getCategories() não seja null, ou seja, que o usuário realmente enviou algo no corpo da requisição.
            //O !newData.getCategories().isEmpty() garante que a lista de categorias não esteja vazia (para evitar sobrescrever com um conjunto vazio sem necessidade).
            product.getCategories().clear();
            //O metodo clear() remove todas as categorias associadas ao produto antes de adicionar as novas. Isso evita que categorias antigas permaneçam no produto.
            product.getCategories().addAll(newData.getCategories());
            //addAll() adiciona todas as novas categorias enviadas no JSON da requisição.
        }

        return productRepository.save(product);
    }

    public void delete(Long id) {
        try {
            productRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("Produto não encontrado: ID " + id);
        }
    }
}
