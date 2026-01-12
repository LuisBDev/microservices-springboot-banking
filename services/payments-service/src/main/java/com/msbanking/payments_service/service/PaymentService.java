package com.msbanking.payments_service.service;

import com.msbanking.payments_service.dto.request.ProcessPaymentRequest;
import com.msbanking.payments_service.dto.response.PaymentResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PaymentService {

    PaymentResponse processPayment(ProcessPaymentRequest request);

    PaymentResponse getPaymentById(Long id);

    PaymentResponse getPaymentByTransactionId(String transactionId);

    List<PaymentResponse> getPaymentsByOrderId(Long orderId);

    List<PaymentResponse> getPaymentsByUserId(Long userId);

    PaymentResponse refundPayment(Long paymentId);
}
