package com.example.cyclemartberemake.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MarkRoomAsReadResponse {
    private Long roomId;
    private Integer markedCount;
}
