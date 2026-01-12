package com.msbanking.inventory_service.service;

import com.msbanking.inventory_service.dto.request.CreateProductRequest;
import com.msbanking.inventory_service.dto.request.ReserveStockRequest;
import com.msbanking.inventory_service.dto.request.UpdateProductRequest;
import com.msbanking.inventory_service.dto.response.ProductResponse;
import com.msbanking.inventory_service.dto.response.ProductStockAvailabilityResponse;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse getProductById(Long id);

    ProductResponse getProductBySku(String sku);

    List<ProductResponse> getAllProducts();

    ProductResponse updateProduct(Long id, UpdateProductRequest request);

    void deleteProduct(Long id);

    ProductResponse reserveStock(ReserveStockRequest request);

    ProductResponse releaseStock(ReserveStockRequest request);

    ProductStockAvailabilityResponse checkStockAvailability(String sku, Integer quantity);
}
