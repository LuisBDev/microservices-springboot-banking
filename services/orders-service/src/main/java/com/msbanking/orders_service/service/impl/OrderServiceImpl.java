package com.msbanking.orders_service.service.impl;

import com.msbanking.orders_service.dto.request.CreateOrderRequest;
import com.msbanking.orders_service.dto.response.OrderResponse;
import com.msbanking.orders_service.entity.Order;
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

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    public OrderServiceImpl(OrderRepository orderRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for user ID: {}", request.getUserId());

        // TODO: Implementar la lógica completa de orquestación con Feign clients
        // 1. Validar usuario existe (Users MS)
        // 2. Verificar stock (Inventory MS)
        // 3. Reservar stock (Inventory MS)
        // 4. Procesar pago (Payments MS)
        // 5. Confirmar o cancelar orden

        Order order = Order.builder()
                .userId(request.getUserId())
                .totalAmount(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);

        log.info("Order created successfully with ID: {}", savedOrder.getId());
        return orderMapper.toResponse(savedOrder);
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
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        log.info("Cancelling order with ID: {}", id);

        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + id));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed order");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);

        // TODO: Liberar stock en Inventory MS
        // TODO: Procesar reembolso en Payments MS si ya se procesó el pago

        Order cancelledOrder = orderRepository.save(order);

        log.info("Order cancelled successfully with ID: {}", id);
        return orderMapper.toResponse(cancelledOrder);
    }
}
