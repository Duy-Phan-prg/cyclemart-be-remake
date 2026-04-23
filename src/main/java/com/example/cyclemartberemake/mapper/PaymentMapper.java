package com.example.cyclemartberemake.mapper;

import com.example.cyclemartberemake.dto.response.PaymentResponse;
import com.example.cyclemartberemake.entity.Payment;
import com.example.cyclemartberemake.entity.PaymentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "status", target = "statusDisplay", qualifiedByName = "mapPaymentStatus")
    @Mapping(source = "bikePost.id", target = "bikePostId")
    @Mapping(source = "bikePost.title", target = "bikeTitle")
    @Mapping(source = "bikePost.price", target = "bikePrice")
    @Mapping(source = "bikePost.user.fullName", target = "sellerName")
    @Mapping(source = "bikePost.user.phone", target = "sellerPhone")
    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponseList(List<Payment> payments);

    @org.mapstruct.Named("mapPaymentStatus")
    default String mapPaymentStatus(PaymentStatus status) {
        if (status == null) return null;
        return switch (status) {
            case PENDING -> "Đang xử lý";
            case SUCCESS -> "Thành công";
            case FAILED -> "Thất bại";
            case REFUNDED -> "Đã hoàn tiền";
            case CANCELLED -> "Đã hủy";
        };
    }
}