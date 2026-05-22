package com.techton.activity.dto;

import com.techton.activity.ActivityStatus;

public record ActivityRejectResponse(
        Long activityId,
        ActivityStatus status,
        String reason
) {
}
