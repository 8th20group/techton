package com.techton.shop.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.techton.coach.Coach;
import com.techton.coach.dto.CoachSummary;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RandomBoxResponse(
        String result,
        Integer rewardPoint,
        CoachSummary coach,
        String message,
        int currentPoint
) {

    public static RandomBoxResponse point(int rewardPoint, int currentPoint) {
        return new RandomBoxResponse(
                "POINT",
                rewardPoint,
                null,
                rewardPoint + "포인트를 획득했습니다!",
                currentPoint
        );
    }

    public static RandomBoxResponse coachTicket(Coach coach, int currentPoint) {
        return new RandomBoxResponse(
                "COACH_TICKET",
                null,
                CoachSummary.from(coach),
                coach.getName() + " 코치 이용권을 획득했습니다!",
                currentPoint
        );
    }

    public static RandomBoxResponse lose(int currentPoint) {
        return new RandomBoxResponse(
                "LOSE",
                0,
                null,
                "아쉽지만 꽝입니다.",
                currentPoint
        );
    }
}
