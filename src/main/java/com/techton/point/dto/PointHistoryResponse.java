package com.techton.point.dto;

import com.techton.point.PointHistory;
import com.techton.point.PointHistoryType;
import java.time.LocalDateTime;

public record PointHistoryResponse(
        PointHistoryType type,
        int amount,
        String reason,
        LocalDateTime createdAt
) {

    public static PointHistoryResponse from(PointHistory history) {
        return new PointHistoryResponse(
                history.getType(),
                history.getAmount(),
                history.getReason(),
                history.getCreatedAt()
        );
    }
}
