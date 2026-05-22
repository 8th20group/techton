package com.techton.ticket;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCrewIdOrderByCreatedAtDesc(Long crewId);

    boolean existsByCrewIdAndTypeAndCreatedAtBetween(
            Long crewId,
            TicketType type,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
            select t.coachId, count(t.id)
            from Ticket t
            where t.status = com.techton.ticket.TicketStatus.USED
            group by t.coachId
            """)
    List<Object[]> countUsedTicketsGroupByCoachId();
}
