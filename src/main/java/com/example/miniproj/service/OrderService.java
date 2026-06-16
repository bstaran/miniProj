package com.example.miniproj.service;

import com.example.miniproj.dto.order.OrderCreateRequest;
import com.example.miniproj.dto.order.OrderResponse;
import com.example.miniproj.entity.Order;
import com.example.miniproj.entity.Product;
import com.example.miniproj.exception.InsufficientStockException;
import com.example.miniproj.exception.InvalidOrderQuantityException;
import com.example.miniproj.exception.OrderNotFoundException;
import com.example.miniproj.exception.ProductNotFoundException;
import com.example.miniproj.repository.OrderRepository;
import com.example.miniproj.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        validateQuantity(request.quantity());

        int updatedCount = productRepository.decreaseStock(request.productId(), request.quantity());
        if (updatedCount == 0) {
            if (!productRepository.existsById(request.productId())) {
                throw new ProductNotFoundException(request.productId());
            }
            throw new InsufficientStockException(request.productId());
        }

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));
        Order order = new Order(product, request.quantity());
        Order savedOrder = orderRepository.save(order);
        return OrderResponse.from(savedOrder);
    }

    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return OrderResponse.from(order);
    }

    public Page<OrderResponse> getOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(OrderResponse::from);
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidOrderQuantityException();
        }
    }
}
