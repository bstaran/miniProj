package com.example.miniproj.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createProduct() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Keyboard",
                                  "price": 30000,
                                  "stockQuantity": 10
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.price").value(30000))
                .andExpect(jsonPath("$.stockQuantity").value(10));
    }

    @Test
    void getProducts() throws Exception {
        createProduct("Mouse", 15000, 20);
        createProduct("Monitor", 200000, 5);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Mouse"))
                .andExpect(jsonPath("$[1].name").value("Monitor"));
    }

    @Test
    void getProduct() throws Exception {
        Long productId = createProduct("Desk", 120000, 3);

        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Desk"))
                .andExpect(jsonPath("$.price").value(120000))
                .andExpect(jsonPath("$.stockQuantity").value(3));
    }

    @Test
    void updateProduct() throws Exception {
        Long productId = createProduct("Chair", 70000, 7);

        mockMvc.perform(put("/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Office Chair",
                                  "price": 90000,
                                  "stockQuantity": 4
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Office Chair"))
                .andExpect(jsonPath("$.price").value(90000))
                .andExpect(jsonPath("$.stockQuantity").value(4));
    }

    @Test
    void deleteProduct() throws Exception {
        Long productId = createProduct("Lamp", 25000, 11);

        mockMvc.perform(delete("/products/{id}", productId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMissingProductReturnsNotFound() throws Exception {
        mockMvc.perform(get("/products/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found. id=999"));
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

        String idField = "\"id\":";
        int start = response.indexOf(idField) + idField.length();
        int end = response.indexOf(",", start);
        return Long.parseLong(response.substring(start, end));
    }
}
