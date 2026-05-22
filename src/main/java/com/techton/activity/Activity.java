package com.techton.activity;

import com.techton.crew.Crew;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Crew crew;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;

    @Column(nullable = false)
    private int point;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityStatus status;

    private String evidenceUrl;

    private String memo;

    @Column(nullable = false)
    private LocalDate activityDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String rejectReason;

    public Activity(Crew crew, ActivityType type, int point, ActivityStatus status,
                    String evidenceUrl, String memo, LocalDate activityDate) {
        this.crew = crew;
        this.type = type;
        this.point = point;
        this.status = status;
        this.evidenceUrl = evidenceUrl;
        this.memo = memo;
        this.activityDate = activityDate;
        this.createdAt = LocalDateTime.now();
    }

    public void approve(int point) {
        this.status = ActivityStatus.APPROVED;
        this.point = point;
        this.rejectReason = null;
    }

    public void reject(String reason) {
        this.status = ActivityStatus.REJECTED;
        this.rejectReason = reason;
    }
}
