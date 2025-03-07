package com.e_commerce.E_commerce.App.repository;

import com.e_commerce.E_commerce.App.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductsRepository extends MongoRepository<Product, String> {
}
