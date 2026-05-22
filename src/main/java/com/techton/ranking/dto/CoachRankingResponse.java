package com.techton.ranking.dto;

public record CoachRankingResponse(
        int rank,
        String coachName,
        long usedCount
) {

    public CoachRankingResponse withRank(int rank) {
        return new CoachRankingResponse(rank, coachName, usedCount);
    }
}
