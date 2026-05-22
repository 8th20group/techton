package com.techton.shop.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.techton.shop.ShopItemType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ShopItemResponse(
        Long itemId,
        String name,
        String type,
        int price,
        Integer weeklyLimit,
        String description
) {

    public static ShopItemResponse from(ShopItemType item) {
        return new ShopItemResponse(
                item.getItemId(),
                item.getDisplayName(),
                item.name(),
                item.getPrice(),
                item.getWeeklyLimit(),
                item.getDescription()
        );
    }
}
