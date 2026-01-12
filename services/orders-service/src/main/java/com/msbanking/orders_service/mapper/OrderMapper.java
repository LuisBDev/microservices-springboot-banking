package com.msbanking.orders_service.mapper;

import com.msbanking.orders_service.dto.response.OrderItemResponse;
import com.msbanking.orders_service.dto.response.OrderResponse;
import com.msbanking.orders_service.entity.Order;
import com.msbanking.orders_service.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    OrderResponse toResponse(Order order);

    OrderItemResponse toItemResponse(OrderItem orderItem);
}
