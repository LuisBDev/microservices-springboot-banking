package com.msbanking.orders_service.service;

import com.msbanking.orders_service.dto.request.CreateOrderRequest;
import com.msbanking.orders_service.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {
    
    OrderResponse createOrder(CreateOrderRequest request);
    
    OrderResponse getOrderById(Long id);
    
    List<OrderResponse> getOrdersByUserId(Long userId);
    
    OrderResponse cancelOrder(Long id);
}
