package com.techton.ticket.dto;

import com.techton.ticket.Ticket;
import com.techton.ticket.TicketStatus;
import com.techton.ticket.TicketType;
import java.time.LocalDateTime;

public record TicketResponse(
        Long ticketId,
        TicketType type,
        String coachName,
        TicketStatus status,
        LocalDateTime createdAt
) {

    public static TicketResponse of(Ticket ticket, String coachName) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getType(),
                coachName,
                ticket.getStatus(),
                ticket.getCreatedAt()
        );
    }
}
