package com.example.shop.bottomup;

import com.example.shop.order.Order;
import com.example.shop.order.OrderRepository;
import com.example.shop.order.OrderStatus;
import com.example.shop.product.Product;
import com.example.shop.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Восходящее тестирование, этап 1: репозитории и тестовая БД (H2). */
@DataJpaTest
class Stage1RepositoryIT {

    @Autowired ProductRepository products;
    @Autowired OrderRepository orders;

    @Test
    void saveAndFindProductByName() {
        products.save(new Product("Книга", 500, 10));

        assertThat(products.findByName("Книга")).isPresent()
                .get().extracting(Product::getPrice).isEqualTo(500L);
    }

    @Test
    void duplicateProductName_violatesUniqueConstraint() {
        products.saveAndFlush(new Product("Книга", 500, 10));

        assertThatThrownBy(() -> products.saveAndFlush(new Product("Книга", 700, 3)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void orderIsSavedWithProductAndDefaultStatus() {
        Product p = products.save(new Product("Ручка", 50, 100));

        Order saved = orders.save(new Order(p, 3, 150));

        assertThat(orders.findById(saved.getId())).isPresent();
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.NEW);
    }
}
