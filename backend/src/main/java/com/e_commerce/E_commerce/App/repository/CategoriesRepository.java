package com.e_commerce.E_commerce.App.repository;

import com.e_commerce.E_commerce.App.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriesRepository extends MongoRepository<Category, String> {
}