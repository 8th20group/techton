package com.techton.activity.dto;

import com.techton.activity.ActivityStatus;

public record ActivitySubmitResponse(
        Long activityId,
        ActivityStatus status,
        String message
) {
}
