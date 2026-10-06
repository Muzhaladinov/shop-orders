package com.example.shop.topdown;

import com.example.shop.common.ApiExceptionHandler;
import com.example.shop.order.Order;
import com.example.shop.order.OrderController;
import com.example.shop.order.OrderRepository;
import com.example.shop.order.OrderService;
import com.example.shop.product.Product;
import com.example.shop.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Нисходящее тестирование, этап 2: реальные контроллер и сервис, репозитории заменены заглушками. */
@WebMvcTest(OrderController.class)
@Import({OrderService.class, ApiExceptionHandler.class})
class Stage2ControllerRealServiceIT {

    @Autowired MockMvc mvc;
    @MockitoBean ProductRepository productRepository;
    @MockitoBean OrderRepository orderRepository;

    private Product product(int stock) {
        Product p = new Product("Книга", 500, stock);
        ReflectionTestUtils.setField(p, "id", 1L);
        return p;
    }

    private String body(long productId, int qty) {
        return "{\"productId\":" + productId + ",\"quantity\":" + qty + "}";
    }

    @Test
    void createOrder_ok_calculatesTotal() throws Exception {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product(10)));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            ReflectionTestUtils.setField(o, "id", 5L);
            return o;
        });

        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body(1, 2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(1000))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    void createOrder_unknownProduct_returns404() throws Exception {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body(99, 1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void createOrder_notEnoughStock_returns409() throws Exception {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product(1)));

        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body(1, 5)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("BUSINESS_RULE"));
    }

    @Test
    void createOrder_zeroQuantity_returns409() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body(1, 0)))
                .andExpect(status().isConflict());
    }

    @Test
    void cancelOrder_unknownOrder_returns404() throws Exception {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        mvc.perform(post("/api/orders/404/cancel"))
                .andExpect(status().isNotFound());
    }
}
