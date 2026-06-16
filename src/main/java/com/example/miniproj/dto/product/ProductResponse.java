package com.example.miniproj.dto.product;

import com.example.miniproj.entity.Product;

public record ProductResponse(
        Long id,
        String name,
        Integer price,
        Integer stockQuantity
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStockQuantity()
        );
    }
}
