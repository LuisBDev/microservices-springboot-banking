package com.msbanking.payments_service.mapper;

import com.msbanking.payments_service.dto.request.ProcessPaymentRequest;
import com.msbanking.payments_service.dto.response.PaymentResponse;
import com.msbanking.payments_service.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    PaymentResponse toResponse(Payment payment);

    Payment toEntity(ProcessPaymentRequest request);
}
