package com.e_commerce.E_commerce.App.controllers;

import com.e_commerce.E_commerce.App.model.Category;
import com.e_commerce.E_commerce.App.service.CategoriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoriesController {
    private final CategoriesService categoriesService;

    @Autowired
    public CategoriesController(CategoriesService categoriesService) {
        this.categoriesService = categoriesService;
    }

    @GetMapping
    public List<Category> getAllCategories() {
        return categoriesService.getAllCategories();
    }
}
