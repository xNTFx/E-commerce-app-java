package com.e_commerce.E_commerce.App.service;

import com.e_commerce.E_commerce.App.model.Product;
import com.e_commerce.E_commerce.App.repository.ProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductsService {

    private final ProductsRepository productsRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public ProductsService(ProductsRepository productsRepository, MongoTemplate mongoTemplate) {
        this.productsRepository = productsRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<Product> getProducts(int offset, int limit, String sortBy, Double minPrice, Double maxPrice, List<String> categoryList) {
        // Validate pagination parameters
        if (offset < 0) {
            throw new IllegalArgumentException("Offset cannot be negative");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than zero");
        }
        // Validate price range: maxPrice must not be lower than minPrice
        if (minPrice != null && maxPrice != null && maxPrice < minPrice) {
            throw new IllegalArgumentException("maxPrice cannot be lower than minPrice");
        }

        Query query = new Query();

        // Filter by price if minPrice or maxPrice is provided
        if (minPrice != null || maxPrice != null) {
            Criteria priceCriteria = Criteria.where("price");
            if (minPrice != null) {
                priceCriteria = priceCriteria.gte(minPrice);
            }
            if (maxPrice != null) {
                priceCriteria = priceCriteria.lte(maxPrice);
            }
            query.addCriteria(priceCriteria);
        }

        // Filter by categories if category list is provided
        if (categoryList != null && !categoryList.isEmpty()) {
            query.addCriteria(Criteria.where("category").in(categoryList));
        }

        query.with(getSortOrder(sortBy));

        query.skip(offset).limit(limit);

        return mongoTemplate.find(query, Product.class);
    }

    private Sort getSortOrder(String sortBy) {
        if (sortBy == null || sortBy.isEmpty()) {
            return Sort.by(Sort.Direction.ASC, "_id"); // Domyślne sortowanie A-Z po tytule
        }

        return switch (sortBy) {
            case "price-asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price-desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "name-az" -> Sort.by(Sort.Direction.ASC, "title");
            case "name-za" -> Sort.by(Sort.Direction.DESC, "title");
            default -> Sort.by(Sort.Direction.ASC, "_id"); // Domyślne sortowanie
        };
    }

    public long getTotalProductsCount(Double minPrice, Double maxPrice, List<String> categoryList) {
        Query query = new Query();

        // Filter by price
        if (minPrice != null || maxPrice != null) {
            Criteria priceCriteria = new Criteria("price");
            if (minPrice != null) {
                priceCriteria = priceCriteria.gte(minPrice);
            }
            if (maxPrice != null) {
                priceCriteria = priceCriteria.lte(maxPrice);
            }
            query.addCriteria(priceCriteria);
        }

        // Filter by category
        if (categoryList != null && !categoryList.isEmpty()) {
            query.addCriteria(Criteria.where("category").in(categoryList));
        }

        return mongoTemplate.count(query, Product.class);
    }

    public Optional<Product> getProductById(String id) {
        return productsRepository.findById(id);
    }

    public List<Product> getProductsByIds(List<String> ids) {
        Query query = new Query(Criteria.where("_id").in(ids));
        return mongoTemplate.find(query, Product.class);
    }
}
