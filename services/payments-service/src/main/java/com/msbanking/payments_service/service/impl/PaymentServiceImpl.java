package com.msbanking.payments_service.service.impl;

import com.msbanking.payments_service.dto.request.ProcessPaymentRequest;
import com.msbanking.payments_service.dto.response.PaymentResponse;
import com.msbanking.payments_service.entity.Payment;
import com.msbanking.payments_service.enums.PaymentStatus;
import com.msbanking.payments_service.mapper.PaymentMapper;
import com.msbanking.payments_service.repository.PaymentRepository;
import com.msbanking.payments_service.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    public static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    public PaymentServiceImpl(PaymentRepository paymentRepository, PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
    }

    @Override
    @Transactional
    public PaymentResponse processPayment(ProcessPaymentRequest request) {
        log.info("Processing payment for order ID: {} with amount: {}", request.getOrderId(), request.getAmount());

        Payment payment = paymentMapper.toEntity(request);
        payment.setTransactionId(generateTransactionId());

        // Simular procesamiento de pago (95% éxito)
        boolean paymentSuccess = Math.random() < 0.95;

        if (paymentSuccess) {
            payment.setStatus(PaymentStatus.COMPLETED);
            log.info("Payment processed successfully with transaction ID: {}", payment.getTransactionId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            log.warn("Payment processing failed for order ID: {}", request.getOrderId());
        }

        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        log.info("Fetching payment with ID: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with ID: " + id));
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByTransactionId(String transactionId) {
        log.info("Fetching payment with transaction ID: {}", transactionId);
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with transaction ID: " + transactionId));
        return paymentMapper.toResponse(payment);
    }


    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrderId(Long orderId) {
        log.info("Fetching payments for order ID: {}", orderId);
        return paymentRepository.findByOrderId(orderId)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        log.info("Fetching payments for user ID: {}", userId);
        return paymentRepository.findByUserId(userId)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(Long paymentId) {
        log.info("Processing refund for payment ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with ID: " + paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Only completed payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        Payment refundedPayment = paymentRepository.save(payment);

        log.info("Payment refunded successfully for payment ID: {}", paymentId);
        return paymentMapper.toResponse(refundedPayment);
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID();
    }
}
