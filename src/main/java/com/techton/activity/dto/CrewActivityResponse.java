package com.techton.activity.dto;

import com.techton.activity.Activity;
import com.techton.activity.ActivityStatus;
import com.techton.activity.ActivityType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CrewActivityResponse(
        Long activityId,
        ActivityType type,
        int point,
        ActivityStatus status,
        String evidenceUrl,
        String memo,
        LocalDate activityDate,
        LocalDateTime createdAt,
        String rejectReason
) {

    public static CrewActivityResponse from(Activity activity) {
        return new CrewActivityResponse(
                activity.getId(),
                activity.getType(),
                activity.getPoint(),
                activity.getStatus(),
                activity.getEvidenceUrl(),
                activity.getMemo(),
                activity.getActivityDate(),
                activity.getCreatedAt(),
                activity.getRejectReason()
        );
    }
}
