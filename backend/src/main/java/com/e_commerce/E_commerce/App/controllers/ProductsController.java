package com.e_commerce.E_commerce.App.controllers;

import com.e_commerce.E_commerce.App.model.Product;
import com.e_commerce.E_commerce.App.service.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/products")
public class ProductsController {

    private final ProductsService productsService;

    @Autowired
    public ProductsController(ProductsService productsService) {
        this.productsService = productsService;
    }

    @GetMapping
    public Map<String, Object> getProducts(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) List<String> categoryList
    ) {
        List<Product> products = productsService.getProducts(offset, limit, sortBy, minPrice, maxPrice, categoryList);
        long totalCount = productsService.getTotalProductsCount(minPrice, maxPrice, categoryList);

        Map<String, Object> response = new HashMap<>();
        response.put("data", products);
        response.put("totalCount", totalCount);

        return response;
    }

    @GetMapping("/singleProduct")
    public Product getSingleProduct(@RequestParam String id) {
        return productsService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @GetMapping("/productsFromIdArray")
    public List<Product> getProductsFromIdArray(@RequestParam List<String> ids) {
        return productsService.getProductsByIds(ids);
    }
}
