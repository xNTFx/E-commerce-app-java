package com.e_commerce.E_commerce.App.repository;

import com.e_commerce.E_commerce.App.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdersRepository extends MongoRepository<Order, String> {
    List<Order> findByUserId(String userId);
}
