package com.techton.auth.dto;

import com.techton.crew.Track;

public record MeResponse(
        Long crewId,
        String githubId,
        String nickname,
        int generation,
        Track track,
        int point
) {
}
