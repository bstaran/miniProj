package com.example.miniproj.exception;

public class InvalidOrderQuantityException extends RuntimeException {

    public InvalidOrderQuantityException() {
        super("Order quantity must be greater than 0.");
    }
}
