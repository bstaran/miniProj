package com.example.miniproj.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createOrder() throws Exception {
        Long productId = createProduct("Keyboard", 30000, 10);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d
                                }
                                """.formatted(productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.productName").value("Keyboard"));
    }

    @Test
    void getOrder() throws Exception {
        Long productId = createProduct("Mouse", 15000, 20);
        Long orderId = createOrder(productId);

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.productName").value("Mouse"));
    }

    @Test
    void getOrderShowsUpdatedProductName() throws Exception {
        Long productId = createProduct("Monitor", 200000, 5);
        Long orderId = createOrder(productId);

        mockMvc.perform(put("/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Gaming Monitor",
                                  "price": 220000,
                                  "stockQuantity": 4
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.productName").value("Gaming Monitor"));
    }

    @Test
    void createOrderWithMissingProductReturnsNotFound() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 999
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found. id=999"));
    }

    @Test
    void getMissingOrderReturnsNotFound() throws Exception {
        mockMvc.perform(get("/orders/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order not found. id=999"));
    }

    private Long createProduct(String name, int price, int stockQuantity) throws Exception {
        String response = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "price": %d,
                                  "stockQuantity": %d
                                }
                                """.formatted(name, price, stockQuantity)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return extractId(response);
    }

    private Long createOrder(Long productId) throws Exception {
        String response = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d
                                }
                                """.formatted(productId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return extractId(response);
    }

    private Long extractId(String response) {
        String idField = "\"id\":";
        int start = response.indexOf(idField) + idField.length();
        int end = response.indexOf(",", start);
        return Long.parseLong(response.substring(start, end));
    }
}
