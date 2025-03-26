package com.criando.projeto.services;

import com.criando.projeto.entities.Category;
import com.criando.projeto.entities.User;
import com.criando.projeto.repositories.CategoryRepository;
import com.criando.projeto.services.exceptions.DatabaseException;
import com.criando.projeto.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServices {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(Long id) {
        Optional <Category> obj =  categoryRepository.findById(id);
        return obj.get();
    }

    public Category insert(Category obj) {
        return categoryRepository.save(obj);
    }


    public Category update(Long id, Category obj) {
        try {
            Category entity = categoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(id));
            updateData(entity, obj);
            return categoryRepository.save(entity);
        } catch (ResourceNotFoundException e) {
            throw e; // Garante que o erro 404 seja propagado corretamente
        }
    }

    private void updateData(Category entity, Category obj) {
        entity.setName(obj.getName());
    }

    public void delete(Long id) {
        try {
            categoryRepository.deleteById(id); }
        catch (EmptyResultDataAccessException e) {throw new ResourceNotFoundException(id);} //Você tentou excluir um usuário com um id que não foi encontrado.
        catch (DataIntegrityViolationException e) {throw new DatabaseException(e.getMessage());} // Essa exceção ocorre quando a tentativa de exclusão viola uma restrição de integridade no banco de dados. Isso pode acontecer, por exemplo, quando um registro está sendo referenciado por outro
    }
}
