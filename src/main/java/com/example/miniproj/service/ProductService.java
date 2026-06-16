package com.example.miniproj.service;

import com.example.miniproj.dto.product.ProductCreateRequest;
import com.example.miniproj.dto.product.ProductResponse;
import com.example.miniproj.dto.product.ProductUpdateRequest;
import com.example.miniproj.entity.Product;
import com.example.miniproj.exception.ProductNotFoundException;
import com.example.miniproj.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        Product product = new Product(request.name(), request.price(), request.stockQuantity());
        Product savedProduct = productRepository.save(product);
        return ProductResponse.from(savedProduct);
    }

    public List<ProductResponse> getProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .toList();
    }

    public ProductResponse getProduct(Long id) {
        Product product = findProduct(id);
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = findProduct(id);
        product.update(request.name(), request.price(), request.stockQuantity());
        return ProductResponse.from(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProduct(id);
        productRepository.delete(product);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
