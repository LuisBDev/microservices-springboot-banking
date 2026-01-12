package com.msbanking.inventory_service.controller;

import com.msbanking.inventory_service.dto.request.CreateProductRequest;
import com.msbanking.inventory_service.dto.request.ReserveStockRequest;
import com.msbanking.inventory_service.dto.request.UpdateProductRequest;
import com.msbanking.inventory_service.dto.response.ProductResponse;
import com.msbanking.inventory_service.dto.response.ProductStockAvailabilityResponse;
import com.msbanking.inventory_service.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> getProductBySku(@PathVariable String sku) {
        ProductResponse response = productService.getProductBySku(sku);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> response = productService.getAllProducts();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reserve")
    public ResponseEntity<ProductResponse> reserveStock(@Valid @RequestBody ReserveStockRequest request) {
        ProductResponse response = productService.reserveStock(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/release")
    public ResponseEntity<ProductResponse> releaseStock(@Valid @RequestBody ReserveStockRequest request) {
        ProductResponse response = productService.releaseStock(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-stock/{sku}/{quantity}")
    public ResponseEntity<ProductStockAvailabilityResponse> checkStockAvailability(
            @PathVariable String sku,
            @PathVariable Integer quantity) {
        
        ProductStockAvailabilityResponse productStockAvailabilityResponse = productService.checkStockAvailability(sku, quantity);
        return ResponseEntity.ok(productStockAvailabilityResponse);
    }
}
