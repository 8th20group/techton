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

    public static PointHistoryResponse from(PointHistory pointHistory) {
        return new PointHistoryResponse(
                pointHistory.getType(),
                pointHistory.getAmount(),
                pointHistory.getReason(),
                pointHistory.getCreatedAt()
        );
    }
}
