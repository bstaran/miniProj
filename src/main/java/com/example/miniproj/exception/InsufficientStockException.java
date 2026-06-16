package com.example.miniproj.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(Long productId) {
        super("Insufficient stock. productId=" + productId);
    }
}
