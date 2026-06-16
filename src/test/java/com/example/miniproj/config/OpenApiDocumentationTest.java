package com.example.miniproj.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getOpenApiDocs() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("MiniProj API"))
                .andExpect(jsonPath("$.info.description").value("Product and order management API"))
                .andExpect(jsonPath("$.info.version").value("0.0.1-SNAPSHOT"))
                .andExpect(jsonPath("$.paths['/products']").exists())
                .andExpect(jsonPath("$.paths['/products/{id}']").exists())
                .andExpect(jsonPath("$.paths['/orders']").exists())
                .andExpect(jsonPath("$.paths['/orders/{id}']").exists());
    }
}
