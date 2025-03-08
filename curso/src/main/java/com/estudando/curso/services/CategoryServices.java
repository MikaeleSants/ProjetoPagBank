package com.estudando.curso.services;

import com.estudando.curso.entities.Category;
import com.estudando.curso.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
}
