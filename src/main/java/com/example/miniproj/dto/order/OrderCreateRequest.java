package com.example.miniproj.dto.order;

public record OrderCreateRequest(
        Long productId,
        Integer quantity
) {
}
