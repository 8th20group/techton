package com.techton.ticket;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCrewIdOrderByCreatedAtDesc(Long crewId);

    boolean existsByCrewIdAndTypeAndCreatedAtBetween(
            Long crewId,
            TicketType type,
            LocalDateTime start,
            LocalDateTime end
    );
}
