package com.techton.point.dto;

import java.util.Map;

public record PointSummaryResponse(
        int totalPoint,
        int weeklyEarnedPoint,
        int weeklyLimitPoint,
        Map<String, Integer> activities
) {
}
