package com.techton.activity.dto;

import com.techton.activity.ActivityStatus;

public record ActivityApprovalResponse(
        Long activityId,
        ActivityStatus status,
        int earnedPoint
) {
}
