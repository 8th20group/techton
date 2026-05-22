package com.techton.ticket;

import lombok.Getter;

@Getter
public enum TicketType {
    CAFE(100),
    MEAL(300);

    private final int price;

    TicketType(int price) {
        this.price = price;
    }
}
