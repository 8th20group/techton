package com.techton.coach.dto;

import com.techton.coach.Coach;
import com.techton.crew.Track;

public record CoachSummary(
        String name,
        Track track
) {

    public static CoachSummary from(Coach coach) {
        return new CoachSummary(coach.getName(), coach.getTrack());
    }
}
