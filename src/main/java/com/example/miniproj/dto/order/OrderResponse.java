package com.example.miniproj.dto.order;

import com.example.miniproj.entity.Order;

public record OrderResponse(
        Long id,
        Long productId,
        String productName
) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getProduct().getId(),
                order.getProduct().getName()
        );
    }
}
