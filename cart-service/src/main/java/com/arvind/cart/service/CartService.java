package com.arvind.cart.service;

import com.arvind.cart.dto.AddToCartRequest;
import com.arvind.cart.dto.CartResponse;

public interface CartService {

    CartResponse getCart(String userId);

    CartResponse addToCart(String userId, AddToCartRequest request);

    CartResponse removeFromCart(String userId, Long medicineId);

    void clearCart(String userId);
}