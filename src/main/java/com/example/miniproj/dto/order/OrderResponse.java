package com.example.miniproj.dto.order;

import com.example.miniproj.entity.Order;

public record OrderResponse(
        Long id,
        Long productId,
        String productName,
        Integer quantity
) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getProduct().getId(),
                order.getProduct().getName(),
                order.getQuantity()
        );
    }
}
