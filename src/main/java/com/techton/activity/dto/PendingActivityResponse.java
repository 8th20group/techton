package com.techton.activity.dto;

import com.techton.activity.Activity;
import com.techton.activity.ActivityStatus;
import com.techton.activity.ActivityType;

public record PendingActivityResponse(
        Long activityId,
        Long crewId,
        String nickname,
        ActivityType type,
        String evidenceUrl,
        String memo,
        ActivityStatus status
) {

    public static PendingActivityResponse from(Activity activity) {
        return new PendingActivityResponse(
                activity.getId(),
                activity.getCrew().getId(),
                activity.getCrew().getNickname(),
                activity.getType(),
                activity.getEvidenceUrl(),
                activity.getMemo(),
                activity.getStatus()
        );
    }
}
