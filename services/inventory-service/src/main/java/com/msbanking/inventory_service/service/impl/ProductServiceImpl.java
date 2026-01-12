package com.msbanking.inventory_service.service.impl;

import com.msbanking.inventory_service.dto.request.CreateProductRequest;
import com.msbanking.inventory_service.dto.request.ReserveStockRequest;
import com.msbanking.inventory_service.dto.request.UpdateProductRequest;
import com.msbanking.inventory_service.dto.response.ProductResponse;
import com.msbanking.inventory_service.dto.response.ProductStockAvailabilityResponse;
import com.msbanking.inventory_service.entity.Product;
import com.msbanking.inventory_service.mapper.ProductMapper;
import com.msbanking.inventory_service.repository.ProductRepository;
import com.msbanking.inventory_service.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating product with SKU: {}", request.getSku());

        if (productRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("Product with SKU " + request.getSku() + " already exists");
        }

        Product product = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);

        log.info("Product created successfully with ID: {}", savedProduct.getId());
        return productMapper.toResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        log.info("Fetching product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        log.info("Fetching product with SKU: {}", sku);
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with SKU: " + sku));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        log.info("Updating product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

        productMapper.updateEntityFromRequest(request, product);
        Product updatedProduct = productRepository.save(product);

        log.info("Product updated successfully with ID: {}", id);
        return productMapper.toResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);

        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found with ID: " + id);
        }

        productRepository.deleteById(id);
        log.info("Product deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional
    public ProductResponse reserveStock(ReserveStockRequest request) {
        log.info("Reserving {} units of product with SKU: {}", request.getQuantity(), request.getSku());

        Product product = productRepository.findBySku(request.getSku())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with SKU: " + request.getSku()));

        if (product.getQuantity() < request.getQuantity()) {
            throw new IllegalStateException("Insufficient stock for product: " + request.getSku() +
                    ". Available: " + product.getQuantity() + ", Requested: " + request.getQuantity());
        }

        product.setQuantity(product.getQuantity() - request.getQuantity());
        Product updatedProduct = productRepository.save(product);

        log.info("Stock reserved successfully. New quantity: {}", updatedProduct.getQuantity());
        return productMapper.toResponse(updatedProduct);
    }

    @Override
    @Transactional
    public ProductResponse releaseStock(ReserveStockRequest request) {
        log.info("Releasing {} units of product with SKU: {}", request.getQuantity(), request.getSku());

        Product product = productRepository.findBySku(request.getSku())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with SKU: " + request.getSku()));

        product.setQuantity(product.getQuantity() + request.getQuantity());
        Product updatedProduct = productRepository.save(product);

        log.info("Stock released successfully. New quantity: {}", updatedProduct.getQuantity());
        return productMapper.toResponse(updatedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductStockAvailabilityResponse checkStockAvailability(String sku, Integer quantity) {
        log.info("Checking stock availability for SKU: {} with quantity: {}", sku, quantity);

        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with SKU: " + sku));

        boolean available = product.getQuantity() >= quantity;
        log.info("Stock availability for SKU {}: {}", sku, available);

        return ProductStockAvailabilityResponse.builder()
                .sku(sku)
                .requestedQuantity(quantity)
                .available(available)
                .build();

    }
}
