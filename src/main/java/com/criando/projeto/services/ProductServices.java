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
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada. ID: " + categoryId));
        product.getCategories().add(category);
        return productRepository.save(product);
    }


    public Product update(Long id, Product obj) {
        try {
            Product entity = productRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado. ID: " + id)); // Lança 404 se não encontrar
            updateData(entity, obj);
            return productRepository.save(entity);
        } catch (ResourceNotFoundException e) {
            throw e;
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
            product.getCategories().clear();
            product.getCategories().addAll(newData.getCategories());
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
