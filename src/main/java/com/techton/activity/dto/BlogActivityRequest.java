package com.techton.activity.dto;

public record BlogActivityRequest(
        String activityDate,
        String blogUrl,
        String memo
) {
}
