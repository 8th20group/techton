package com.techton.shop.dto;

import com.techton.ticket.TicketType;

public record CoachTicketRequest(
        TicketType ticketType
) {
}
