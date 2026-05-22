package com.techton.crew.dto;

import com.techton.crew.Crew;
import com.techton.crew.Track;

public record CrewResponse(
        Long id,
        String githubId,
        String nickname,
        int generation,
        Track track,
        int point
) {

    public static CrewResponse from(Crew crew) {
        return new CrewResponse(
                crew.getId(),
                crew.getGithubId(),
                crew.getNickname(),
                crew.getGeneration(),
                crew.getTrack(),
                crew.getPoint()
        );
    }
}
