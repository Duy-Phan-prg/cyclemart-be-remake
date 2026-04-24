package com.example.cyclemartberemake.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatMessageRequest {
    @NotNull
    private Long roomId;

    @NotBlank
    private String content;
}
