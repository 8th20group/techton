package com.techton.activity;

import lombok.Getter;

@Getter
public enum ActivityType {
    COMMIT("커밋", 5, 35),
    REVIEW("리뷰", 5, 35),
    MISSION("미션", 10, 10),
    BLOG("블로그", 20, 20);

    private final String displayName;
    private final int point;
    private final int weeklyMaxPoint;

    ActivityType(String displayName, int point, int weeklyMaxPoint) {
        this.displayName = displayName;
        this.point = point;
        this.weeklyMaxPoint = weeklyMaxPoint;
    }
}
