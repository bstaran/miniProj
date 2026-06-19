package com.example.miniproj.exception;

public class InvalidOrderQuantityException extends RuntimeException {

    public InvalidOrderQuantityException() {
        super("주문 수량은 1 이상이어야 합니다.");
    }
}
