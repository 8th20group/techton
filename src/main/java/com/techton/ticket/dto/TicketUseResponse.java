package com.techton.ticket.dto;

import com.techton.ticket.Ticket;
import com.techton.ticket.TicketStatus;

public record TicketUseResponse(
        Long ticketId,
        TicketStatus status,
        String message
) {

    public static TicketUseResponse from(Ticket ticket) {
        return new TicketUseResponse(ticket.getId(), ticket.getStatus(), "이용권 사용 완료");
    }
}
