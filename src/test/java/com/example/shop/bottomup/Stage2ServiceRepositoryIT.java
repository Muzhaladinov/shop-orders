package com.example.shop.bottomup;

import com.example.shop.common.ShopExceptions.BusinessRuleException;
import com.example.shop.common.ShopExceptions.NotFoundException;
import com.example.shop.order.Order;
import com.example.shop.order.OrderService;
import com.example.shop.order.OrderStatus;
import com.example.shop.product.Product;
import com.example.shop.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Восходящее тестирование, этап 2: реальный сервис + реальный репозиторий + БД; тест выступает драйвером. */
@DataJpaTest
@Import(OrderService.class)
class Stage2ServiceRepositoryIT {

    @Autowired OrderService service;
    @Autowired ProductRepository products;

    @Test
    void create_savesOrderAndDecreasesStock() {
        Product p = products.save(new Product("Книга", 500, 10));

        Order order = service.create(p.getId(), 2);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getTotal()).isEqualTo(1000);
        assertThat(products.findById(p.getId()).orElseThrow().getStock()).isEqualTo(8);
    }

    @Test
    void create_notEnoughStock_throwsAndKeepsStock() {
        Product p = products.save(new Product("Книга", 500, 1));

        assertThatThrownBy(() -> service.create(p.getId(), 5)).isInstanceOf(BusinessRuleException.class);
        assertThat(products.findById(p.getId()).orElseThrow().getStock()).isEqualTo(1);
    }

    @Test
    void create_unknownProduct_throwsNotFound() {
        assertThatThrownBy(() -> service.create(12345L, 1)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void cancel_restoresStockAndSetsStatus() {
        Product p = products.save(new Product("Книга", 500, 10));
        Order order = service.create(p.getId(), 3);

        Order cancelled = service.cancel(order.getId());

        assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(products.findById(p.getId()).orElseThrow().getStock()).isEqualTo(10);
    }

    @Test
    void cancel_twice_throwsBusinessRule() {
        Product p = products.save(new Product("Книга", 500, 10));
        Order order = service.create(p.getId(), 3);
        service.cancel(order.getId());

        assertThatThrownBy(() -> service.cancel(order.getId())).isInstanceOf(BusinessRuleException.class);
    }
}
