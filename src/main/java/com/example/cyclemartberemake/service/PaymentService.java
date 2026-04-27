package com.example.cyclemartberemake.service;

import com.example.cyclemartberemake.dto.request.CreatePaymentRequest;
import com.example.cyclemartberemake.dto.request.DeliveryUpdateRequest;
import com.example.cyclemartberemake.dto.response.CreatePaymentResponse;
import com.example.cyclemartberemake.dto.response.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface PaymentService {
    

    CreatePaymentResponse createPayment(CreatePaymentRequest request) throws Exception;

    void handleVNPayReturn(Map<String, String> params) throws Exception;

    void handleVNPayIPN(Map<String, String> params) throws Exception;

    Page<PaymentResponse> getPaymentHistory(Pageable pageable);

    Page<PaymentResponse> getPaymentHistoryAsSeller(Pageable pageable);

    Page<PaymentResponse> getPaymentHistoryByStatus(String status, Pageable pageable);

    PaymentResponse getPaymentById(Long id);

    Page<PaymentResponse> getAllPayments(Pageable pageable);
    
    Map<String, Object> getPaymentStatistics();

    void cleanupExpiredPayments();

    PaymentResponse refundPayment(Long paymentId, String reason, Long adminId);
    PaymentResponse getPaymentByOrderId(String orderId);
    PaymentResponse cancelPayment(Long paymentId, String reason);
    PaymentResponse updateOrderStatus(String orderId, String newStatus);

    CreatePaymentResponse createOrderPaymentForNegotiation(Long buyerId, Long bikePostId, Long amount, String description) throws Exception;

    CreatePaymentResponse createInspectionPayment(Long sellerId, Long inspectionId, Double fee) throws Exception;

    PaymentResponse submitDelivery(Long paymentId, Long sellerId, DeliveryUpdateRequest request);

    PaymentResponse confirmReceived(Long paymentId, Long buyerId);

    PaymentResponse cancelRequest(Long paymentId, Long buyerId, String reason);

    PaymentResponse releaseEscrow(Long paymentId, Long adminId, String adminNote);

    PaymentResponse refundEscrow(Long paymentId, Long adminId, String adminNote);

    String generateFreshPaymentUrl(String orderId, Long amount) throws Exception;

    // COD flow: tạo đơn DIRECT_PAYMENT không cần VNPay
    PaymentResponse createDirectPayment(Long buyerId, Long bikePostId, String address, String description);

    // Seller xác nhận đơn COD → chuyển sang PAID_WAITING_DELIVERY
    PaymentResponse sellerConfirmOrder(Long paymentId, Long sellerId);

    // Seller từ chối đơn COD → hủy đơn + ghi nhận lý do
    PaymentResponse sellerRejectOrder(Long paymentId, Long sellerId, String reason);

    // Scheduler: tự động hủy đơn COD quá 24h chưa seller phản hồi
    void autoCancelExpiredSellerConfirmations();
}
