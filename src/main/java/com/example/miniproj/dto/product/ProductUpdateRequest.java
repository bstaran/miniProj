package com.example.miniproj.dto.product;

public record ProductUpdateRequest(
        String name,
        Integer price,
        Integer stockQuantity
) {
}
