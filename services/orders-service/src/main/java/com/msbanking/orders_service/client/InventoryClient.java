package com.msbanking.orders_service.client;

import com.msbanking.orders_service.client.dto.ProductResponse;
import com.msbanking.orders_service.client.dto.ProductStockAvailabilityResponse;
import com.msbanking.orders_service.client.dto.ReserveStockRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @GetMapping("/products/sku/{sku}")
    ProductResponse getProductBySku(@PathVariable("sku") String sku);

    @GetMapping("/products/check-stock/{sku}/{quantity}")
    ProductStockAvailabilityResponse checkStockAvailability(@PathVariable("sku") String sku, @PathVariable("quantity") Integer quantity);

    @PostMapping("/products/reserve")
    ProductResponse reserveStock(@RequestBody ReserveStockRequest request);

    @PostMapping("/products/release")
    ProductResponse releaseStock(@RequestBody ReserveStockRequest request);
}
