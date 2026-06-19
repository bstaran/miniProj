package com.example.miniproj.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
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

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void createOrder() throws Exception {
        Long productId = createProduct("Keyboard", 30000, 10);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": 1
                                }
                                """.formatted(productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.productName").value("Keyboard"))
                .andExpect(jsonPath("$.quantity").value(1));
    }

    @Test
    void createOrderWithoutProductIdReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("상품 ID는 필수입니다."));
    }

    @Test
    void createOrderWithNonPositiveProductIdReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 0,
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("상품 ID는 1 이상이어야 합니다."));
    }

    @Test
    void createOrderWithoutQuantityReturnsBadRequest() throws Exception {
        Long productId = createProduct("Keyboard", 30000, 10);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d
                                }
                                """.formatted(productId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("주문 수량은 필수입니다."));
    }

    @Test
    void createOrderWithNonPositiveQuantityReturnsBadRequest() throws Exception {
        Long productId = createProduct("Keyboard", 30000, 10);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": 0
                                }
                                """.formatted(productId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("주문 수량은 1 이상이어야 합니다."));
    }

    @Test
    void createOrderDecreasesProductStock() throws Exception {
        Long productId = createProduct("Keyboard", 30000, 10);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": 3
                                }
                                """.formatted(productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.productName").value("Keyboard"))
                .andExpect(jsonPath("$.quantity").value(3));

        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(7));
    }

    @Test
    void getOrder() throws Exception {
        Long productId = createProduct("Mouse", 15000, 20);
        Long orderId = createOrder(productId);

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.productName").value("Mouse"))
                .andExpect(jsonPath("$.quantity").value(1));
    }

    @Test
    void getOrders() throws Exception {
        Long keyboardId = createProduct("Keyboard", 30000, 10);
        Long mouseId = createProduct("Mouse", 15000, 20);
        Long keyboardOrderId = createOrder(keyboardId);
        Long mouseOrderId = createOrder(mouseId);

        mockMvc.perform(get("/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(keyboardOrderId))
                .andExpect(jsonPath("$.content[0].productId").value(keyboardId))
                .andExpect(jsonPath("$.content[0].productName").value("Keyboard"))
                .andExpect(jsonPath("$.content[0].quantity").value(1))
                .andExpect(jsonPath("$.content[1].id").value(mouseOrderId))
                .andExpect(jsonPath("$.content[1].productId").value(mouseId))
                .andExpect(jsonPath("$.content[1].productName").value("Mouse"))
                .andExpect(jsonPath("$.content[1].quantity").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getOrdersDoesNotCauseNPlusOne() throws Exception {
        Long keyboardId = createProduct("Keyboard", 30000, 10);
        Long mouseId = createProduct("Mouse", 15000, 20);
        Long monitorId = createProduct("Monitor", 200000, 5);
        createOrder(keyboardId);
        createOrder(mouseId);
        createOrder(monitorId);
        entityManager.flush();
        entityManager.clear();

        Statistics statistics = entityManagerFactory
                .unwrap(SessionFactory.class)
                .getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        mockMvc.perform(get("/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].productName").value("Keyboard"))
                .andExpect(jsonPath("$.content[1].productName").value("Mouse"))
                .andExpect(jsonPath("$.content[2].productName").value("Monitor"));

        assertThat(statistics.getPrepareStatementCount()).isLessThanOrEqualTo(2);
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
                .andExpect(jsonPath("$.productName").value("Gaming Monitor"))
                .andExpect(jsonPath("$.quantity").value(1));
    }

    @Test
    void createOrderWithMissingProductReturnsNotFound() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 999,
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found. id=999"));
    }

    @Test
    void createOrderWithInsufficientStockReturnsBadRequest() throws Exception {
        Long productId = createProduct("Keyboard", 30000, 1);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": 2
                                }
                                """.formatted(productId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient stock. productId=" + productId));

        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(1));
    }

    @Test
    void createOrderTwiceWithOneStockFailsOnSecondOrder() throws Exception {
        Long productId = createProduct("Keyboard", 30000, 1);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": 1
                                }
                                """.formatted(productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(1));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": 1
                                }
                                """.formatted(productId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient stock. productId=" + productId));

        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(0));
    }

    @Test
    void createOrderWithZeroStockReturnsBadRequest() throws Exception {
        Long productId = createProduct("Keyboard", 30000, 0);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": 1
                                }
                                """.formatted(productId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient stock. productId=" + productId));
    }

    @Test
    void createOrderWithInvalidQuantityReturnsBadRequest() throws Exception {
        Long productId = createProduct("Keyboard", 30000, 10);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": 0
                                }
                                """.formatted(productId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("주문 수량은 1 이상이어야 합니다."));
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
                                  "productId": %d,
                                  "quantity": 1
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
