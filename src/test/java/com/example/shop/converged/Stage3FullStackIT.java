package com.example.shop.converged;

import com.example.shop.order.OrderRepository;
import com.example.shop.product.Product;
import com.example.shop.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Сходящееся тестирование: контроллер, сервис, репозиторий и БД работают вместе. */
@SpringBootTest
@AutoConfigureMockMvc
class Stage3FullStackIT {

    @Autowired MockMvc mvc;
    @Autowired ProductRepository products;
    @Autowired OrderRepository orders;

    @BeforeEach
    void clean() {
        orders.deleteAll();
        products.deleteAll();
    }

    private String body(long productId, int qty) {
        return "{\"productId\":" + productId + ",\"quantity\":" + qty + "}";
    }

    @Test
    void createGetCancel_fullFlow_persistsChanges() throws Exception {
        Product p = products.save(new Product("Книга", 500, 10));

        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body(p.getId(), 2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(1000));
        assertThat(orders.count()).isEqualTo(1);
        assertThat(products.findById(p.getId()).orElseThrow().getStock()).isEqualTo(8);

        Long orderId = orders.findAll().get(0).getId();
        mvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEW"));

        mvc.perform(post("/api/orders/" + orderId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        assertThat(products.findById(p.getId()).orElseThrow().getStock()).isEqualTo(10);
    }

    @Test
    void createOrder_unknownProduct_returns404() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body(999, 1)))
                .andExpect(status().isNotFound());
        assertThat(orders.count()).isZero();
    }

    @Test
    void createOrder_notEnoughStock_returns409AndStockUnchanged() throws Exception {
        Product p = products.save(new Product("Книга", 500, 1));

        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body(p.getId(), 5)))
                .andExpect(status().isConflict());
        assertThat(products.findById(p.getId()).orElseThrow().getStock()).isEqualTo(1);
        assertThat(orders.count()).isZero();
    }

    @Test
    void createProduct_thenListIt() throws Exception {
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ручка\",\"price\":50,\"stock\":100}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ручка"));

        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stock").value(100));
    }
}
