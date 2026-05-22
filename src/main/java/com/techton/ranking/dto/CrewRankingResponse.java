package com.techton.ranking.dto;

import com.techton.crew.Crew;

public record CrewRankingResponse(
        int rank,
        String nickname,
        String githubId,
        int point
) {

    public static CrewRankingResponse of(int rank, Crew crew) {
        return new CrewRankingResponse(
                rank,
                crew.getNickname(),
                crew.getGithubId(),
                crew.getPoint()
        );
    }
}
