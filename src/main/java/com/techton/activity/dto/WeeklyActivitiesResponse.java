package com.techton.activity.dto;

import java.time.LocalDate;
import java.util.List;

public record WeeklyActivitiesResponse(
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        int weeklyEarnedPoint,
        int weeklyLimitPoint,
        List<WeeklyActivityItemResponse> items
) {
}
