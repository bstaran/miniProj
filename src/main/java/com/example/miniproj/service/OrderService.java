package com.example.miniproj.service;

import com.example.miniproj.dto.order.OrderCreateRequest;
import com.example.miniproj.dto.order.OrderResponse;
import com.example.miniproj.entity.Order;
import com.example.miniproj.entity.Product;
import com.example.miniproj.exception.OrderNotFoundException;
import com.example.miniproj.exception.ProductNotFoundException;
import com.example.miniproj.repository.OrderRepository;
import com.example.miniproj.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));
        Order order = new Order(product);
        Order savedOrder = orderRepository.save(order);
        return OrderResponse.from(savedOrder);
    }

    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return OrderResponse.from(order);
    }
}
