package com.criando.projeto.services;

import com.criando.projeto.entities.Product;
import com.criando.projeto.repositories.ProductRepository;
import com.criando.projeto.services.exceptions.DatabaseException;
import com.criando.projeto.services.exceptions.ResourceNotFoundException;
import com.criando.projeto.specifications.ProductSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServices {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> findAll(Specification<Product> spec) {
        return productRepository.findAll(spec);
    }

    public Product findById(Long id) {
        Optional <Product> obj =  productRepository.findById(id);
        return obj.get();
    }
    public Product insert(Product obj) {
        return productRepository.save(obj);
    }
    public void delete(Long id) {
        try {
            productRepository.deleteById(id); } catch (EmptyResultDataAccessException e)
        {throw new ResourceNotFoundException(id);} catch (DataIntegrityViolationException e)
        {throw new DatabaseException(e.getMessage());}
    }
    //precisei atualizar esse trecho com a ajuda do chatgpt, porque o do curso estava desatualizado
    public Product update(Long id, Product obj) {
        try {
            Product entity = productRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(id)); // Lança 404 se não encontrar
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
}
