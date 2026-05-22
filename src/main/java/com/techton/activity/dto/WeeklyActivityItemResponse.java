package com.techton.activity.dto;

import com.techton.activity.ActivityType;

public record WeeklyActivityItemResponse(
        ActivityType type,
        String name,
        int point,
        int earnedCount,
        int maxCount,
        int earnedPoint,
        int maxPoint
) {
}
