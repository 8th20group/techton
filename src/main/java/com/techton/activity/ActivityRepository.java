package com.techton.activity;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    boolean existsByCrewIdAndTypeAndStatusAndActivityDate(
            Long crewId,
            ActivityType type,
            ActivityStatus status,
            LocalDate activityDate
    );

    boolean existsByCrewIdAndTypeAndStatusInAndActivityDateBetween(
            Long crewId,
            ActivityType type,
            List<ActivityStatus> statuses,
            LocalDate startDate,
            LocalDate endDate
    );

    int countByCrewIdAndTypeAndStatusAndActivityDateBetween(
            Long crewId,
            ActivityType type,
            ActivityStatus status,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Activity> findByCrewIdAndStatusAndActivityDateBetween(
            Long crewId,
            ActivityStatus status,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Activity> findByStatus(ActivityStatus status);

    @Query("""
            select coalesce(sum(a.point), 0)
            from Activity a
            where a.crew.id = :crewId
              and a.status = :status
              and a.activityDate between :startDate and :endDate
            """)
    int sumPointByCrewIdAndStatusAndActivityDateBetween(
            @Param("crewId") Long crewId,
            @Param("status") ActivityStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
