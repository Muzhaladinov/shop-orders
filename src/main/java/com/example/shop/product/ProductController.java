package com.example.shop.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    public record CreateProductRequest(@NotBlank String name, @Min(1) long price, @Min(0) int stock) {}

    private final ProductRepository products;

    public ProductController(ProductRepository products) {
        this.products = products;
    }

    @GetMapping
    public List<Product> list() {
        return products.findAll();
    }

    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody CreateProductRequest req) {
        Product saved = products.save(new Product(req.name(), req.price(), req.stock()));
        return ResponseEntity.created(URI.create("/api/products/" + saved.getId())).body(saved);
    }
}
