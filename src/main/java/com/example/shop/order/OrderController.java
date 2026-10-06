package com.example.shop.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    public record CreateOrderRequest(@NotNull Long productId, @NotNull Integer quantity) {}

    public record OrderResponse(Long id, Long productId, int quantity, long total, String status) {
        static OrderResponse of(Order o) {
            return new OrderResponse(o.getId(), o.getProduct().getId(), o.getQuantity(), o.getTotal(), o.getStatus().name());
        }
    }

    private final OrderService service;

    public OrderController(OrderService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest req) {
        Order order = service.create(req.productId(), req.quantity());
        return ResponseEntity.created(URI.create("/api/orders/" + order.getId())).body(OrderResponse.of(order));
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return OrderResponse.of(service.get(id));
    }

    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponse cancel(@PathVariable Long id) {
        return OrderResponse.of(service.cancel(id));
    }
}
