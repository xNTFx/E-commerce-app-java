package com.e_commerce.E_commerce.App.controllers;

import com.e_commerce.E_commerce.App.model.Order;
import com.e_commerce.E_commerce.App.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    private final OrdersService ordersService;

    @Autowired
    public OrdersController(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @GetMapping("/getOrders")
    public List<Order> getOrders(@RequestParam String idToken) throws Exception {
        String userId = ordersService.getFirebaseAuthService().verifyIdToken(idToken);
        return ordersService.getOrders(userId);
    }

    @PostMapping("/createOrder")
    public Order createOrder(@RequestBody Map<String, Object> requestBody) throws Exception {
        return ordersService.processOrder(requestBody);
    }

    @GetMapping("/getOrderDetails/{orderId}")
    public ResponseEntity<?> getOrderDetails(@PathVariable String orderId) {
        try {
            Order enrichedOrder = ordersService.getEnrichedOrder(orderId);
            if (enrichedOrder == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Order not found"));
            }
            return ResponseEntity.ok(enrichedOrder);
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
