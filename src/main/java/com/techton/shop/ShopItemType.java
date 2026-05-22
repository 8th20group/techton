package com.techton.shop;

import lombok.Getter;

@Getter
public enum ShopItemType {

    RANDOM_BOX(1L, "랜덤 박스", 10, null, "확률에 따라 포인트 또는 코치 이용권을 획득합니다."),
    COACH_CAFE_TICKET(2L, "코치 이용권 - 카페", 100, 1, null),
    COACH_MEAL_TICKET(3L, "코치 이용권 - 식사", 300, 1, null);

    private final Long itemId;
    private final String displayName;
    private final int price;
    private final Integer weeklyLimit;
    private final String description;

    ShopItemType(Long itemId, String displayName, int price, Integer weeklyLimit, String description) {
        this.itemId = itemId;
        this.displayName = displayName;
        this.price = price;
        this.weeklyLimit = weeklyLimit;
        this.description = description;
    }
}
