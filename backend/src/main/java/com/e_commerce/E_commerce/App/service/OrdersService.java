package com.e_commerce.E_commerce.App.service;

import com.e_commerce.E_commerce.App.model.CartItem;
import com.e_commerce.E_commerce.App.model.Order;
import com.e_commerce.E_commerce.App.model.Person;
import com.e_commerce.E_commerce.App.model.Product;
import com.e_commerce.E_commerce.App.model.ProductWithCount;
import com.e_commerce.E_commerce.App.repository.OrdersRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final CartItemsService cartItemsService;
    private final ProductsService productsService;
    private final FirebaseAuthService firebaseAuthService;

    @Autowired
    public OrdersService(OrdersRepository ordersRepository,
                         CartItemsService cartItemsService,
                         ProductsService productsService,
                         FirebaseAuthService firebaseAuthService) {
        this.ordersRepository = ordersRepository;
        this.cartItemsService = cartItemsService;
        this.productsService = productsService;
        this.firebaseAuthService = firebaseAuthService;
    }

    public List<Order> getOrders(String userId) {
        return ordersRepository.findByUserId(userId);
    }

    public Optional<Order> getOrderById(String orderId) {
        return ordersRepository.findById(orderId);
    }

    public Order createOrder(String userId, Order orderData) {
        List<ProductWithCount> validProducts = orderData.getProducts();
        if (validProducts == null || validProducts.isEmpty()) {
            throw new IllegalStateException("Order must contain at least one product.");
        }
        int totalQuantity = validProducts.stream().mapToInt(ProductWithCount::getCount).sum();
        double totalPrice = validProducts.stream().mapToDouble(ProductWithCount::getTotalPrice).sum();

        orderData.setUserId(userId);
        orderData.setTotal(totalPrice);
        orderData.setQuantity(totalQuantity);

        Order savedOrder = ordersRepository.save(orderData);
        if (!"anonymous".equals(userId)) {
            cartItemsService.deleteAllCartItems(userId);
        }
        return savedOrder;
    }

    public Order processOrder(Map<String, Object> requestBody) throws Exception {
        // idToken Verification - if empty, treat as guest
        String idToken = (String) requestBody.get("idToken");
        String userId;
        boolean isGuest = false;
        if (idToken == null || idToken.isEmpty()) {
            isGuest = true;
            userId = "anonymous";
        } else {
            userId = firebaseAuthService.verifyIdToken(idToken);
        }

        // For logged-in users, retrieve products from the cart
        List<CartItem> cartItems = new ArrayList<>();
        if (!isGuest) {
            cartItems = cartItemsService.getCartItems(userId);
        }

        Object stateObject = requestBody.get("state");
        Map<String, Object> stateMap = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        if (stateObject instanceof String) {
            stateMap = mapper.readValue((String) stateObject, new TypeReference<Map<String, Object>>() {});
        } else if (stateObject instanceof Map) {
            stateMap = (Map<String, Object>) stateObject;
        } else {
            throw new IllegalArgumentException("state should be a map or valid JSON");
        }

        if (stateMap.containsKey("state") && stateMap.get("state") instanceof Map) {
            stateMap = (Map<String, Object>) stateMap.get("state");
        }

        Person userDetails;
        if (stateMap.containsKey("userDetails")) {
            userDetails = mapper.convertValue(stateMap.get("userDetails"), Person.class);
        } else if (stateMap.containsKey("name") && stateMap.containsKey("surname")) {
            userDetails = mapper.convertValue(stateMap, Person.class);
        } else {
            throw new IllegalArgumentException("User details are missing in the state");
        }

        // Retrieve the list of products
        List<Map<String, Object>> productsList = new ArrayList<>();
        if (stateMap.containsKey("products") && stateMap.get("products") instanceof List<?>) {
            productsList = ((List<?>) stateMap.get("products"))
                    .stream()
                    .filter(item -> item instanceof Map)
                    .map(item -> (Map<String, Object>) item)
                    .collect(Collectors.toList());
        }
        // If productsList is empty and the user is logged in, use cart items
        if (productsList.isEmpty() && !isGuest) {
            for (CartItem cartItem : cartItems) {
                Map<String, Object> productData = new HashMap<>();
                productData.put("productId", cartItem.getProductId());
                productData.put("count", cartItem.getCount());
                productsList.add(productData);
            }
        }

        if (productsList.isEmpty()) {
            throw new IllegalStateException(isGuest
                    ? "No products in the order. For guest orders, please include the 'products' field in state."
                    : "No products in the order state.");
        }

        List<ProductWithCount> validProducts = new ArrayList<>();
        for (Map<String, Object> productData : productsList) {
            String productId = (String) productData.get("productId");
            if (productId == null || productId.isEmpty()) {
                continue;
            }
            int count = productData.get("count") instanceof Integer ? (int) productData.get("count") : 1;
            Optional<Product> optionalProduct = productsService.getProductById(productId);
            if (optionalProduct.isEmpty()) {
                continue;
            }
            Product product = optionalProduct.get();
            ProductWithCount productWithCount = new ProductWithCount();
            productWithCount.set_id(product.get_id());
            productWithCount.setId(product.getId());
            productWithCount.setTitle(product.getTitle());
            productWithCount.setDescription(product.getDescription());
            productWithCount.setPrice(product.getPrice());
            productWithCount.setDiscountPercentage(product.getDiscountPercentage());
            productWithCount.setRating(product.getRating());
            productWithCount.setStock(product.getStock());
            productWithCount.setBrand(product.getBrand());
            productWithCount.setCategory(product.getCategory());
            productWithCount.setThumbnail(product.getThumbnail());
            productWithCount.setImages(product.getImages());
            productWithCount.setCount(count);
            productWithCount.setTotalPrice(count * product.getPrice());
            validProducts.add(productWithCount);
        }
        if (validProducts.isEmpty()) {
            throw new IllegalStateException("No available products found in the order!");
        }

        int totalQuantity = validProducts.stream().mapToInt(ProductWithCount::getCount).sum();
        double totalPrice = validProducts.stream().mapToDouble(ProductWithCount::getTotalPrice).sum();

        Order orderData = new Order();
        orderData.setUserId(userId);
        orderData.setProducts(validProducts);
        orderData.setTotal(totalPrice);
        orderData.setQuantity(totalQuantity);
        orderData.setCreateDate(new Date());

        orderData.setName(userDetails.getName());
        orderData.setSurname(userDetails.getSurname());
        orderData.setAddress(userDetails.getAddress());
        orderData.setZipCode(userDetails.getZipCode());
        orderData.setCityTown(userDetails.getCityTown());
        orderData.setPhone(userDetails.getPhone());
        orderData.setEmail(userDetails.getEmail());

        Order savedOrder = ordersRepository.save(orderData);
        if (!isGuest) {
            cartItemsService.deleteAllCartItems(userId);
        }
        return savedOrder;
    }

    public Order getEnrichedOrder(String orderId) {
        Optional<Order> optionalOrder = getOrderById(orderId);
        if (optionalOrder.isEmpty()) {
            return null;
        }
        Order order = optionalOrder.get();
        List<String> productIds = order.getProducts().stream()
                .map(ProductWithCount::get_id)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (productIds.isEmpty()) {
            throw new IllegalArgumentException("Invalid order: products missing product id");
        }
        List<Product> products = productsService.getProductsByIds(productIds);
        if (products.isEmpty()) {
            throw new IllegalArgumentException("Invalid order: products not found");
        }
        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::get_id, product -> product));
        order.setProducts(order.getProducts().stream().map(pwc -> {
            Product product = productMap.get(pwc.get_id());
            if (product != null) {
                pwc.setTitle(product.getTitle());
                pwc.setDescription(product.getDescription());
                pwc.setPrice(product.getPrice());
                pwc.setDiscountPercentage(product.getDiscountPercentage());
                pwc.setRating(product.getRating());
                pwc.setStock(product.getStock());
                pwc.setBrand(product.getBrand());
                pwc.setCategory(product.getCategory());
                pwc.setThumbnail(product.getThumbnail());
                pwc.setImages(product.getImages());
                pwc.setTotalPrice(pwc.getCount() * product.getPrice());
            }
            return pwc;
        }).collect(Collectors.toList()));
        return order;
    }

    public FirebaseAuthService getFirebaseAuthService() {
        return firebaseAuthService;
    }
}
