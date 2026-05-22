package com.techton.shop.dto;

import com.techton.coach.Coach;
import com.techton.coach.dto.CoachSummary;
import com.techton.ticket.Ticket;
import com.techton.ticket.TicketType;

public record CoachTicketResponse(
        Long ticketId,
        TicketType ticketType,
        CoachSummary coach,
        int price,
        String message,
        int currentPoint
) {

    public static CoachTicketResponse of(Ticket ticket, Coach coach, int currentPoint) {
        String ticketName = ticket.getType() == TicketType.CAFE ? "카페" : "식사";
        return new CoachTicketResponse(
                ticket.getId(),
                ticket.getType(),
                CoachSummary.from(coach),
                ticket.getType().getPrice(),
                coach.getName() + " 코치 " + ticketName + " 이용권을 획득했습니다.",
                currentPoint
        );
    }
}
