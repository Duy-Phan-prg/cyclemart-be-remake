package com.example.cyclemartberemake.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatRoomRequest {
    @NotNull
    private Long bikePostId;
}
