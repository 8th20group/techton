package com.techton.ticket;

import com.techton.global.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long crewId;

    @Column(nullable = false)
    private Long coachId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime usedAt;

    public Ticket(Long crewId, Long coachId, TicketType type) {
        this.crewId = crewId;
        this.coachId = coachId;
        this.type = type;
        this.status = TicketStatus.AVAILABLE;
        this.createdAt = LocalDateTime.now();
    }

    public void use() {
        if (this.status == TicketStatus.USED) {
            throw new BusinessException("이미 사용한 이용권입니다.");
        }
        this.status = TicketStatus.USED;
        this.usedAt = LocalDateTime.now();
    }
}
