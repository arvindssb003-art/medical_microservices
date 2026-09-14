package com.arvind.medical.inventory.exception;

public class InventoryNotFoundException extends RuntimeException {

    public InventoryNotFoundException(String message) {
        super(message);
    }
}