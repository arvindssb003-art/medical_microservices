package com.arvind.cart.controller;

import com.arvind.cart.dto.AddToCartRequest;
import com.arvind.cart.dto.CartResponse;
import com.arvind.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping({"/me", "/{userId}"})
    public ResponseEntity<CartResponse> getCart(
            @PathVariable(required = false) String userId,
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(
                cartService.getCart(resolveUserId(userId, jwt))
        );
    }

    @PostMapping({"/me/items", "/{userId}/items"})
    public ResponseEntity<CartResponse> addToCart(
            @PathVariable(required = false) String userId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddToCartRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cartService.addToCart(resolveUserId(userId, jwt), request));
    }

    @DeleteMapping({"/me/items/{medicineId}", "/{userId}/items/{medicineId}"})
    public ResponseEntity<CartResponse> removeFromCart(
            @PathVariable(required = false) String userId,
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long medicineId) {

        return ResponseEntity.ok(
                cartService.removeFromCart(resolveUserId(userId, jwt), medicineId)
        );
    }

    @DeleteMapping({"/me", "/{userId}"})
    public ResponseEntity<Void> clearCart(
            @PathVariable(required = false) String userId,
            @AuthenticationPrincipal Jwt jwt) {

        cartService.clearCart(resolveUserId(userId, jwt));

        return ResponseEntity.noContent().build();
    }

    private String resolveUserId(String requestedUserId, Jwt jwt) {
        String authenticatedUserId = jwt.getSubject();
        if (requestedUserId != null && !requestedUserId.equals(authenticatedUserId)) {
            throw new AccessDeniedException("Customers may only access their own cart");
        }
        return authenticatedUserId;
    }
}