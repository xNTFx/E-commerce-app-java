package com.e_commerce.E_commerce.App.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cart_items")
public class CartItem {
    @Id
    private String _id;
    private String userId;
    private String productId;
    private int count;

    // Getters
    public String getId() {
        return _id;
    }

    public String getUserId() {
        return userId;
    }

    public String getProductId() {
        return productId;
    }

    public int getCount() {
        return count;
    }

    // Setters
    public void setId(String _id) {
        this._id = _id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
