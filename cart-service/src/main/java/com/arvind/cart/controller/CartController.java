package com.arvind.cart.controller;

import com.arvind.cart.dto.AddToCartRequest;
import com.arvind.cart.dto.CartResponse;
import com.arvind.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getCart(
            @PathVariable String userId) {

        return ResponseEntity.ok(
                cartService.getCart(userId)
        );
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<CartResponse> addToCart(
            @PathVariable String userId,
            @Valid @RequestBody AddToCartRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cartService.addToCart(userId, request));
    }

    @DeleteMapping("/{userId}/items/{medicineId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @PathVariable String userId,
            @PathVariable Long medicineId) {

        return ResponseEntity.ok(
                cartService.removeFromCart(userId, medicineId)
        );
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(
            @PathVariable String userId) {

        cartService.clearCart(userId);

        return ResponseEntity.noContent().build();
    }
}