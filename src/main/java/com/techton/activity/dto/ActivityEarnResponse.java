package com.techton.activity.dto;

import com.techton.activity.ActivityType;

public record ActivityEarnResponse(
        ActivityType activityType,
        int earnedPoint,
        String message
) {
}
