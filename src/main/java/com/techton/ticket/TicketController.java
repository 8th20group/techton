package com.techton.ticket;

import com.techton.ticket.dto.TicketResponse;
import com.techton.ticket.dto.TicketUseResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/crews/{crewId}/tickets")
    public List<TicketResponse> findByCrew(@PathVariable Long crewId) {
        return ticketService.findByCrew(crewId);
    }

    @PatchMapping("/crews/{crewId}/tickets/{ticketId}/use")
    public TicketUseResponse use(@PathVariable Long crewId, @PathVariable Long ticketId) {
        return ticketService.use(crewId, ticketId);
    }
}
