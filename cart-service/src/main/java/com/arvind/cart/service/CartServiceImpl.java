package com.arvind.cart.service;

import com.arvind.cart.client.InventoryClient;
import com.arvind.cart.client.MedicineClient;
import com.arvind.cart.dto.AddToCartRequest;
import com.arvind.cart.dto.CartItemResponse;
import com.arvind.cart.dto.CartResponse;
import com.arvind.cart.dto.InventoryResponse;
import com.arvind.cart.dto.MedicineResponse;
import com.arvind.cart.entity.Cart;
import com.arvind.cart.entity.CartItem;
import com.arvind.cart.exception.CartNotFoundException;
import com.arvind.cart.repository.CartItemRepository;
import com.arvind.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arvind.cart.exception.InsufficientStockException;
import com.arvind.cart.exception.MedicineNotFoundException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    private final MedicineClient medicineClient;
    private final InventoryClient inventoryClient;

    @Override
    @Transactional
    public CartResponse addToCart(
            String userId,
            AddToCartRequest request) {

        MedicineResponse medicine =
                medicineClient.getMedicine(request.getMedicineId());

        if (!medicine.isActive()) {
            throw new MedicineNotFoundException(
                    "Medicine is not available"
            );
        }

        InventoryResponse inventory =
                inventoryClient.getInventory(request.getMedicineId());

        if (inventory.getAvailableQuantity()
                < request.getQuantity()) {

            throw new InsufficientStockException(
                    "Insufficient medicine stock"
            );
        }

        Cart cart = cartRepository
                .findByUserId(userId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder()
                                .userId(userId)
                                .totalAmount(BigDecimal.ZERO)
                                .build()
                ));

        CartItem cartItem = cartItemRepository
                .findByCartIdAndMedicineId(
                        cart.getId(),
                        request.getMedicineId()
                )
                .orElse(null);

        if (cartItem != null) {

            int newQuantity =
                    cartItem.getQuantity() + request.getQuantity();

            if (inventory.getAvailableQuantity() < newQuantity) {
                throw new InsufficientStockException(
                        "Requested quantity exceeds available stock"
                );
            }

            cartItem.setQuantity(newQuantity);

            cartItem.setSubtotal(
                    medicine.getPrice()
                            .multiply(
                                    BigDecimal.valueOf(newQuantity)
                            )
            );

        } else {

            BigDecimal subtotal =
                    medicine.getPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            request.getQuantity()
                                    )
                            );

            cartItem = CartItem.builder()
                    .medicineId(request.getMedicineId())
                    .quantity(request.getQuantity())
                    .price(medicine.getPrice())
                    .subtotal(subtotal)
                    .build();

            cart.addItem(cartItem);
        }

        cartItemRepository.save(cartItem);

        updateCartTotal(cart);

        return buildCartResponse(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String userId) {

        Cart cart = cartRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "Cart not found for user: " + userId
                        )
                );

        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeFromCart(
            String userId,
            Long medicineId) {

        Cart cart = cartRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "Cart not found for user: " + userId
                        )
                );

        CartItem cartItem = cartItemRepository
                .findByCartIdAndMedicineId(
                        cart.getId(),
                        medicineId
                )
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "Medicine not found in cart"
                        )
                );

        cart.removeItem(cartItem);

        updateCartTotal(cart);

        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public void clearCart(String userId) {

        Cart cart = cartRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "Cart not found for user: " + userId
                        )
                );

        cart.getItems().forEach(item -> item.setCart(null));
        cart.getItems().clear();

        cart.setTotalAmount(BigDecimal.ZERO);

        cartRepository.save(cart);
    }

    private void updateCartTotal(Cart cart) {

        BigDecimal total = cart.getItems()
                .stream()
                .map(CartItem::getSubtotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        cart.setTotalAmount(total);

        cartRepository.save(cart);
    }

    private CartResponse buildCartResponse(Cart cart) {

        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(item -> new CartItemResponse(
                        item.getId(),
                        item.getMedicineId(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getSubtotal()
                ))
                .toList();

        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                items,
                cart.getTotalAmount()
        );
    }
}