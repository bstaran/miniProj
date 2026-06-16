package com.example.miniproj.dto.product;

public record ProductCreateRequest(
        String name,
        Integer price,
        Integer stockQuantity
) {
}
