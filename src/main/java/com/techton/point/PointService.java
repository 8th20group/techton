package com.techton.point;

import com.techton.activity.Activity;
import com.techton.activity.ActivityRepository;
import com.techton.activity.ActivityStatus;
import com.techton.activity.ActivityType;
import com.techton.crew.Crew;
import com.techton.crew.CrewRepository;
import com.techton.global.BusinessException;
import com.techton.point.dto.PointHistoryResponse;
import com.techton.point.dto.PointSummaryResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private static final int WEEKLY_LIMIT_POINT = 100;

    private final CrewRepository crewRepository;
    private final ActivityRepository activityRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public PointSummaryResponse summary(Long crewId) {
        Crew crew = findCrew(crewId);
        LocalDate weekStartDate = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        Map<String, Integer> activities = Map.of(
                "commit", earnedPoint(crewId, ActivityType.COMMIT, weekStartDate, weekEndDate),
                "review", earnedPoint(crewId, ActivityType.REVIEW, weekStartDate, weekEndDate),
                "mission", earnedPoint(crewId, ActivityType.MISSION, weekStartDate, weekEndDate),
                "blog", earnedPoint(crewId, ActivityType.BLOG, weekStartDate, weekEndDate)
        );
        int weeklyEarnedPoint = activities.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();

        return new PointSummaryResponse(crew.getPoint(), weeklyEarnedPoint, WEEKLY_LIMIT_POINT, activities);
    }

    public List<PointHistoryResponse> histories(Long crewId) {
        findCrew(crewId);
        return pointHistoryRepository.findByCrewIdOrderByCreatedAtDesc(crewId)
                .stream()
                .map(PointHistoryResponse::from)
                .toList();
    }

    private int earnedPoint(Long crewId, ActivityType type, LocalDate weekStartDate, LocalDate weekEndDate) {
        return activityRepository.findByCrewIdAndStatusAndActivityDateBetween(
                        crewId, ActivityStatus.APPROVED, weekStartDate, weekEndDate)
                .stream()
                .filter(activity -> activity.getType() == type)
                .mapToInt(Activity::getPoint)
                .sum();
    }

    private Crew findCrew(Long crewId) {
        return crewRepository.findById(crewId)
                .orElseThrow(() -> new BusinessException("크루를 찾을 수 없습니다."));
    }
}
