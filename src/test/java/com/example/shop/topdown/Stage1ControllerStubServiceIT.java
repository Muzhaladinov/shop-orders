package com.example.shop.topdown;

import com.example.shop.common.ApiExceptionHandler;
import com.example.shop.common.ShopExceptions.NotFoundException;
import com.example.shop.order.Order;
import com.example.shop.order.OrderController;
import com.example.shop.order.OrderService;
import com.example.shop.product.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Нисходящее тестирование, этап 1: реальный контроллер, сервис заменён заглушкой. */
@WebMvcTest(OrderController.class)
@Import(ApiExceptionHandler.class)
class Stage1ControllerStubServiceIT {

    @Autowired MockMvc mvc;
    @MockitoBean OrderService service;

    @Test
    void create_returns201AndJson() throws Exception {
        Product p = new Product("Книга", 500, 10);
        ReflectionTestUtils.setField(p, "id", 1L);
        Order o = new Order(p, 2, 1000);
        ReflectionTestUtils.setField(o, "id", 7L);
        when(service.create(1L, 2)).thenReturn(o);

        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"quantity\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.total").value(1000))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void create_withoutProductId_returns400() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void create_serviceThrowsNotFound_returns404() throws Exception {
        when(service.create(anyLong(), org.mockito.ArgumentMatchers.anyInt()))
                .thenThrow(new NotFoundException("нет такого товара"));

        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":99,\"quantity\":1}"))
                .andExpect(status().isNotFound());
    }
}
