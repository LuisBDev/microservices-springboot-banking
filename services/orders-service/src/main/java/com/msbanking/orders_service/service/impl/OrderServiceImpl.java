package com.msbanking.orders_service.service.impl;

import com.msbanking.commons.exception.BusinessException;
import com.msbanking.commons.exception.ErrorCode;
import com.msbanking.orders_service.client.InventoryClient;
import com.msbanking.orders_service.client.PaymentsClient;
import com.msbanking.orders_service.client.UsersClient;
import com.msbanking.orders_service.client.dto.*;
import com.msbanking.orders_service.dto.request.CreateOrderRequest;
import com.msbanking.orders_service.dto.request.OrderItemRequest;
import com.msbanking.orders_service.dto.response.OrderResponse;
import com.msbanking.orders_service.entity.Order;
import com.msbanking.orders_service.entity.OrderItem;
import com.msbanking.orders_service.enums.OrderStatus;
import com.msbanking.orders_service.mapper.OrderMapper;
import com.msbanking.orders_service.repository.OrderRepository;
import com.msbanking.orders_service.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UsersClient usersClient;
    private final InventoryClient inventoryClient;
    private final PaymentsClient paymentsClient;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderMapper orderMapper,
            UsersClient usersClient,
            InventoryClient inventoryClient,
            PaymentsClient paymentsClient) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.usersClient = usersClient;
        this.inventoryClient = inventoryClient;
        this.paymentsClient = paymentsClient;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for user ID: {}", request.getUserId());

        return validateUser(request.getUserId())
                .flatMap(user -> validateProductsAndStock(request.getItems()))
                .flatMap(products -> createPendingOrder(request, products))
                .map(order -> processOrderWorkflow(order, request))
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_PROCESSING_FAILED));
    }

    private Optional<UserResponse> validateUser(Long userId) {
        try {
            log.debug("Step 1: Validating user exists");
            return Optional.of(usersClient.getUserById(userId));
        } catch (Exception e) {
            log.error("User validation failed for user ID: {}", userId, e);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "User not found with ID: " + userId);
        }
    }

    private Optional<List<ProductResponse>> validateProductsAndStock(List<OrderItemRequest> items) {
        log.debug("Step 2: Validating products and checking stock");

        List<ProductResponse> products = items
                .stream()
                .map(this::validateAndFetchProduct)
                .collect(Collectors.toList());

        return Optional.of(products);
    }

    private ProductResponse validateAndFetchProduct(OrderItemRequest item) {
        try {
            ProductResponse product = inventoryClient.getProductBySku(item.getSku());

            if (Boolean.FALSE.equals(product.getActive())) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_ACTIVE, "Product is not active: " + item.getSku());
            }

            ProductStockAvailabilityResponse stockAvailability = inventoryClient.checkStockAvailability(item.getSku(), item.getQuantity());

            if (Boolean.FALSE.equals(stockAvailability.getAvailable())) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, "Insufficient stock for product: " + item.getSku());
            }

            return product;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Product validation failed for SKU: {}", item.getSku(), e);
            throw new BusinessException(ErrorCode.PRODUCT_VALIDATION_FAILED, "Product validation failed for SKU: " + item.getSku());
        }
    }

    private Optional<Order> createPendingOrder(CreateOrderRequest request, List<ProductResponse> products) {
        log.debug("Step 3-5: Creating order with items");

        BigDecimal totalAmount = calculateTotalAmount(request.getItems(), products);
        log.info("Order total amount: {}", totalAmount);

        Order order = Order.builder()
                .userId(request.getUserId())
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .build();

        List<OrderItem> orderItems = createOrderItems(request.getItems(), products, order);
        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {} and status: PENDING", savedOrder.getId());

        return Optional.of(savedOrder);
    }

    private BigDecimal calculateTotalAmount(List<OrderItemRequest> items, List<ProductResponse> products) {
        BiFunction<OrderItemRequest, ProductResponse, BigDecimal> calculateSubtotal =
                (item, product) -> product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

        return IntStream.range(0, items.size())
                .mapToObj(i -> calculateSubtotal.apply(items.get(i), products.get(i)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<OrderItem> createOrderItems(List<OrderItemRequest> items, List<ProductResponse> products, Order order) {
        return IntStream.range(0, items.size())
                .mapToObj(i -> buildOrderItem(items.get(i), products.get(i), order))
                .collect(Collectors.toList());
    }

    private OrderItem buildOrderItem(OrderItemRequest itemRequest, ProductResponse product, Order order) {
        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

        return OrderItem.builder()
                .order(order)
                .productId(product.getId())
                .sku(product.getSku())
                .productName(product.getName())
                .quantity(itemRequest.getQuantity())
                .unitPrice(product.getPrice())
                .subtotal(subtotal)
                .build();
    }

    private Order processOrderWorkflow(Order order, CreateOrderRequest request) {
        try {
            log.debug("Step 6: Reserving stock for {} items", request.getItems().size());
            reserveStock(request.getItems());
            log.info("Stock reserved successfully for order: {}", order.getId());

            log.debug("Step 7: Processing payment");
            PaymentResponse payment = processPayment(order, request);

            return handlePaymentResult(order, payment, request.getItems());

        } catch (Exception e) {
            log.error("Error processing order: {}. Rolling back...", order.getId(), e);
            rollbackOrder(order, request.getItems());
            throw new BusinessException(ErrorCode.ORDER_PROCESSING_FAILED, "Order processing failed: " + e.getMessage(), e);
        }
    }

    private Order handlePaymentResult(Order order, PaymentResponse payment, List<OrderItemRequest> items) {
        if ("COMPLETED".equals(payment.getStatus())) {
            order.setStatus(OrderStatus.CONFIRMED);
            log.info("Order {} confirmed successfully. Payment transaction: {}", order.getId(), payment.getTransactionId());
        } else {
            log.warn("Payment failed for order: {}. Rolling back...", order.getId());
            releaseStock(items);
            order.setStatus(OrderStatus.CANCELLED);
            throw new BusinessException(ErrorCode.PAYMENT_PROCESSING_FAILED, "Payment processing failed. Order cancelled.");
        }

        return orderRepository.save(order);
    }

    private void rollbackOrder(Order order, List<OrderItemRequest> items) {
        try {
            releaseStock(items);
        } catch (Exception releaseError) {
            log.error("Failed to release stock during rollback for order: {}", order.getId(), releaseError);
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private void reserveStock(List<OrderItemRequest> items) {
        items.forEach(item -> {
            inventoryClient.reserveStock(buildReserveRequest(item));
            log.debug("Reserved {} units of product: {}", item.getQuantity(), item.getSku());
        });
    }

    private void releaseStock(List<OrderItemRequest> items) {

        items.stream()
                .map(this::buildReserveRequest)
                .forEach(request -> {
                    try {
                        inventoryClient.releaseStock(request);
                    } catch (Exception e) {
                        log.error("Failed to release stock for SKU: {}", request.getSku(), e);
                    }
                });


    }

    private ReserveStockRequest buildReserveRequest(OrderItemRequest item) {
        return ReserveStockRequest.builder()
                .sku(item.getSku())
                .quantity(item.getQuantity())
                .build();
    }

    private PaymentResponse processPayment(Order order, CreateOrderRequest request) {
        ProcessPaymentRequest paymentRequest = ProcessPaymentRequest.builder()
                .orderId(order.getId())
                .userId(request.getUserId())
                .amount(order.getTotalAmount())
                .paymentMethod(request.getPaymentMethod())
                .build();

        return paymentsClient.processPayment(paymentRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        log.info("Fetching order with ID: {}", id);
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + id));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        log.info("Fetching orders for user ID: {}", userId);
        return orderRepository.findByUserId(userId)
                .stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        log.info("Cancelling order with ID: {}", id);

        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + id));

        validateCancellationAllowed(order);

        log.debug("Releasing stock for order: {}", id);
        releaseStockForOrderItems(order.getOrderItems());

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);

        log.info("Order cancelled successfully with ID: {}", id);
        return orderMapper.toResponse(cancelledOrder);
    }

    private void validateCancellationAllowed(Order order) {
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_COMPLETED);
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_CANCELLED);
        }
    }

    private void releaseStockForOrderItems(List<OrderItem> orderItems) {
        orderItems.stream()
                .map(this::buildReleaseRequestFromOrderItem)
                .forEach(request -> {
                    try {
                        inventoryClient.releaseStock(request);
                        log.debug("Released {} units of product: {}", request.getQuantity(), request.getSku());
                    } catch (Exception e) {
                        log.error("Failed to release stock for SKU: {} during order cancellation", request.getSku(), e);
                    }
                });
    }

    private ReserveStockRequest buildReleaseRequestFromOrderItem(OrderItem item) {
        return ReserveStockRequest.builder()
                .sku(item.getSku())
                .quantity(item.getQuantity())
                .build();
    }
}
