package com.example.cyclemartberemake.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InspectionRequestDTO {

    @NotNull(message = "Vui lòng chọn xe cần kiểm định")
    private Long postId;

    @NotBlank(message = "Vui lòng nhập địa chỉ xem xe")
    private String address;

    @NotNull(message = "Vui lòng chọn ngày giờ hẹn xem xe")
    @Future(message = "Ngày giờ hẹn kiểm định không được ở trong quá khứ")
    private LocalDateTime scheduledDateTime;

    private String note;
}