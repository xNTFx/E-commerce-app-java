package com.e_commerce.E_commerce.App.controllers;

import com.e_commerce.E_commerce.App.model.CartItem;
import com.e_commerce.E_commerce.App.service.CartItemsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartItemsController {

    private final CartItemsService cartItemsService;

    @Autowired
    public CartItemsController(CartItemsService cartItemsService) {
        this.cartItemsService = cartItemsService;
    }

    @GetMapping
    public List<Map<String, Object>> getCartItems(@RequestParam String userId) throws Exception {
        return cartItemsService.getCartItemsWithDetails(userId);
    }

    @PostMapping("/addItemToCart")
    public CartItem addItemToCart(@RequestBody Map<String, Object> request) throws Exception {
        return cartItemsService.addItemToCart(request);
    }

    @PostMapping("/updateCart")
    public CartItem updateCart(@RequestBody Map<String, Object> request) throws Exception {
        return cartItemsService.updateCart(request);
    }

    @DeleteMapping("/deleteCartItem")
    public void deleteCartItem(@RequestBody Map<String, Object> request) throws Exception {
        cartItemsService.deleteCartItem(request);
    }

    @DeleteMapping("/deleteEntireCart")
    public void deleteEntireCart(@RequestBody Map<String, Object> request) throws Exception {
        cartItemsService.deleteEntireCart(request);
    }
}
