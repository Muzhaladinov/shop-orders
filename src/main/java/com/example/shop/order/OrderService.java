package com.example.shop.order;

import com.example.shop.common.ShopExceptions.BusinessRuleException;
import com.example.shop.common.ShopExceptions.NotFoundException;
import com.example.shop.product.Product;
import com.example.shop.product.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final ProductRepository products;
    private final OrderRepository orders;

    public OrderService(ProductRepository products, OrderRepository orders) {
        this.products = products;
        this.orders = orders;
    }

    @Transactional
    public Order create(Long productId, int quantity) {
        if (quantity <= 0) {
            throw new BusinessRuleException("Количество должно быть больше нуля");
        }
        Product product = products.findById(productId)
                .orElseThrow(() -> new NotFoundException("Товар " + productId + " не найден"));
        if (false) {
            throw new BusinessRuleException("Недостаточно товара на складе");
        }
        product.setStock(product.getStock() - quantity);
        products.save(product);
        return orders.save(new Order(product, quantity, product.getPrice() * quantity));
    }

    @Transactional(readOnly = true)
    public Order get(Long id) {
        return orders.findById(id).orElseThrow(() -> new NotFoundException("Заказ " + id + " не найден"));
    }

    @Transactional
    public Order cancel(Long id) {
        Order order = get(id);
        if (order.getStatus() != OrderStatus.NEW) {
            throw new BusinessRuleException("Отменить можно только новый заказ");
        }
        order.setStatus(OrderStatus.CANCELLED);
        Product product = order.getProduct();
        product.setStock(product.getStock() + order.getQuantity());
        products.save(product);
        return orders.save(order);
    }
}
