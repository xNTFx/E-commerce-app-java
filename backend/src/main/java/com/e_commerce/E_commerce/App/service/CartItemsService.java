package com.e_commerce.E_commerce.App.service;

import com.e_commerce.E_commerce.App.model.CartItem;
import com.e_commerce.E_commerce.App.model.Product;
import com.e_commerce.E_commerce.App.repository.CartItemsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartItemsService {

    private final CartItemsRepository cartItemsRepository;
    private final FirebaseAuthService firebaseAuthService;
    private final ProductsService productsService;

    @Autowired
    public CartItemsService(CartItemsRepository cartItemsRepository,
                            FirebaseAuthService firebaseAuthService,
                            ProductsService productsService) {
        this.cartItemsRepository = cartItemsRepository;
        this.firebaseAuthService = firebaseAuthService;
        this.productsService = productsService;
    }

    public List<CartItem> getCartItems(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("userId cannot be null or empty");
        }
        return cartItemsRepository.findByUserId(userId);
    }

    public List<Map<String, Object>> getCartItemsWithDetails(String idToken) throws Exception {
        String decodedUserId = firebaseAuthService.verifyIdToken(idToken);
        List<CartItem> cartItems = getCartItems(decodedUserId);

        // If cart is empty, return an empty list instead of processing further.
        if (cartItems.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> productIds = cartItems.stream()
                .map(CartItem::getProductId)
                .distinct()
                .collect(Collectors.toList());

        List<Product> products = productsService.getProductsByIds(productIds);
        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::get_id, product -> product));

        return cartItems.stream().map(cartItem -> Map.of(
                "_id", cartItem.getId(),
                "userId", cartItem.getUserId(),
                "productId", cartItem.getProductId(),
                "count", cartItem.getCount(),
                "productDetails", productMap.containsKey(cartItem.getProductId()) ? List.of(productMap.get(cartItem.getProductId())) : List.of()
        )).collect(Collectors.toList());
    }

    public CartItem updateOrAddCartItem(String userId, String productId, int count) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("productId cannot be null or empty");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("count must be greater than 0");
        }
        Optional<CartItem> existingCartItem = cartItemsRepository.findByUserId(userId)
                .stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            cartItem.setCount(cartItem.getCount() + count);
            return cartItemsRepository.save(cartItem);
        } else {
            CartItem newCartItem = new CartItem();
            newCartItem.setUserId(userId);
            newCartItem.setProductId(productId);
            newCartItem.setCount(count);
            return cartItemsRepository.save(newCartItem);
        }
    }

    public CartItem updateCartItem(String userId, String productId, int count) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("productId cannot be null or empty");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("count must be greater than 0");
        }
        Optional<CartItem> existingCartItem = cartItemsRepository.findByUserId(userId)
                .stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            cartItem.setCount(count);
            return cartItemsRepository.save(cartItem);
        } else {
            throw new IllegalArgumentException("Cart item not found for given product");
        }
    }

    public void deleteCartItem(String userId, String cartId) {
        if (cartId == null || cartId.trim().isEmpty()) {
            throw new IllegalArgumentException("cartId cannot be null or empty");
        }
        cartItemsRepository.deleteById(cartId);
    }

    public void deleteAllCartItems(String userId) {
        cartItemsRepository.deleteByUserId(userId);
    }

    public CartItem addItemToCart(Map<String, Object> request) throws Exception {
        if (!request.containsKey("userId") || !request.containsKey("productId") || !request.containsKey("count")) {
            throw new IllegalArgumentException("userId, productId, and count are required");
        }
        String token = (String) request.get("userId");
        String decodedUserId = firebaseAuthService.verifyIdToken(token);
        String productId = (String) request.get("productId");
        int count = (int) request.get("count");
        return updateOrAddCartItem(decodedUserId, productId, count);
    }

    public CartItem updateCart(Map<String, Object> request) throws Exception {
        if (!request.containsKey("userId") || !request.containsKey("productId") || !request.containsKey("count")) {
            throw new IllegalArgumentException("userId, productId, and count are required");
        }
        String token = (String) request.get("userId");
        String decodedUserId = firebaseAuthService.verifyIdToken(token);
        String productId = (String) request.get("productId");
        int count = (int) request.get("count");
        return updateCartItem(decodedUserId, productId, count);
    }

    public void deleteCartItem(Map<String, Object> request) throws Exception {
        if (!request.containsKey("userId") || !request.containsKey("cartId")) {
            throw new IllegalArgumentException("userId and cartId are required");
        }
        String token = (String) request.get("userId");
        String decodedUserId = firebaseAuthService.verifyIdToken(token);
        String cartId = (String) request.get("cartId");
        deleteCartItem(decodedUserId, cartId);
    }

    public void deleteEntireCart(Map<String, Object> request) throws Exception {
        if (!request.containsKey("userId")) {
            throw new IllegalArgumentException("userId is required");
        }
        String token = (String) request.get("userId");
        String decodedUserId = firebaseAuthService.verifyIdToken(token);
        deleteAllCartItems(decodedUserId);
    }
}