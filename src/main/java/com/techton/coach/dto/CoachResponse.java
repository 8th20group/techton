package com.techton.coach.dto;

import com.techton.coach.Coach;
import com.techton.crew.Track;

public record CoachResponse(
        Long id,
        String name,
        Track track
) {

    public static CoachResponse from(Coach coach) {
        return new CoachResponse(coach.getId(), coach.getName(), coach.getTrack());
    }
}
