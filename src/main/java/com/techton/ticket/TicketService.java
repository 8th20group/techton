package com.techton.ticket;

import com.techton.coach.Coach;
import com.techton.coach.CoachRepository;
import com.techton.global.BusinessException;
import com.techton.ticket.dto.TicketResponse;
import com.techton.ticket.dto.TicketUseResponse;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CoachRepository coachRepository;

    @Transactional(readOnly = true)
    public List<TicketResponse> findByCrew(Long crewId) {
        List<Ticket> tickets = ticketRepository.findByCrewIdOrderByCreatedAtDesc(crewId);
        Map<Long, String> coachNames = coachRepository.findAll().stream()
                .collect(Collectors.toMap(Coach::getId, Coach::getName));
        return tickets.stream()
                .map(ticket -> TicketResponse.of(ticket, coachNames.getOrDefault(ticket.getCoachId(), "")))
                .toList();
    }

    public TicketUseResponse use(Long crewId, Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException("이용권을 찾을 수 없습니다."));
        if (!ticket.getCrewId().equals(crewId)) {
            throw new BusinessException("본인의 이용권만 사용할 수 있습니다.");
        }
        ticket.use();
        return TicketUseResponse.from(ticket);
    }
}
