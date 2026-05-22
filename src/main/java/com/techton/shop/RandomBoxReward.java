package com.techton.shop;

import com.techton.global.BusinessException;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;

/**
 * 랜덤 박스 보상 구성. probability 의 총합은 100 이어야 한다.
 */
@Getter
public enum RandomBoxReward {

    LOSE(0, 25),
    POINT_5(5, 25),
    POINT_10(10, 25),
    POINT_20(20, 15),
    POINT_50(50, 9),
    COACH_TICKET(0, 1);

    private final int rewardPoint;
    private final int probability;

    RandomBoxReward(int rewardPoint, int probability) {
        this.rewardPoint = rewardPoint;
        this.probability = probability;
    }

    public static RandomBoxReward draw() {
        int roll = ThreadLocalRandom.current().nextInt(100);
        int cumulative = 0;
        for (RandomBoxReward reward : values()) {
            cumulative += reward.probability;
            if (roll < cumulative) {
                return reward;
            }
        }
        throw new BusinessException("랜덤 박스 확률 구성이 올바르지 않습니다.");
    }

    public boolean isCoachTicket() {
        return this == COACH_TICKET;
    }

    public boolean isLose() {
        return this == LOSE;
    }
}
