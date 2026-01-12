package com.msbanking.orders_service.client;

import com.msbanking.orders_service.client.dto.PaymentResponse;
import com.msbanking.orders_service.client.dto.ProcessPaymentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payments-service")
public interface PaymentsClient {

    @PostMapping("/payments/process")
    PaymentResponse processPayment(@RequestBody ProcessPaymentRequest request);
}
