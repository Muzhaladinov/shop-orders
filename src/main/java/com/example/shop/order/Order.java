package com.example.shop.order;

import com.example.shop.product.Product;
import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Product product;
    @Column(nullable = false)
    private int quantity;
    @Column(nullable = false)
    private long total;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private OrderStatus status = OrderStatus.NEW;

    protected Order() {}

    public Order(Product product, int quantity, long total) {
        this.product = product;
        this.quantity = quantity;
        this.total = total;
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public long getTotal() { return total; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}
