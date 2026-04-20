package com.example.cyclemartberemake.service;

import com.example.cyclemartberemake.dto.request.CreatePaymentRequest;
import com.example.cyclemartberemake.dto.response.CreatePaymentResponse;
import com.example.cyclemartberemake.dto.response.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface PaymentService {
    

    CreatePaymentResponse createPayment(CreatePaymentRequest request) throws Exception;

    void handleIPN(Map<String, Object> data);

    Page<PaymentResponse> getPaymentHistory(Pageable pageable);
    
    Page<PaymentResponse> getPaymentHistoryByStatus(String status, Pageable pageable);

    PaymentResponse getPaymentById(Long id);

    Page<PaymentResponse> getAllPayments(Pageable pageable);
    
    Map<String, Object> getPaymentStatistics();

    void cleanupExpiredPayments();

    PaymentResponse refundPayment(Long paymentId, String reason, Long adminId);
    
    PaymentResponse cancelPayment(Long paymentId, String reason);
}
