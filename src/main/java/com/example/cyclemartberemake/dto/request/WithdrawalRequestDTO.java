package com.example.cyclemartberemake.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WithdrawalRequestDTO {

    @NotNull(message = "Số điểm rút không được để trống")
    @Min(value = 1, message = "Số điểm rút phải lớn hơn 0")
    private Long amount;

    @NotBlank(message = "Tên ngân hàng không được để trống")
    private String bankName;

    @NotBlank(message = "Số tài khoản không được để trống")
    private String accountNumber;

    @NotBlank(message = "Tên chủ tài khoản không được để trống")
    private String accountHolder;
}
