package com.techton.activity;

import com.techton.activity.dto.ActivityApprovalResponse;
import com.techton.activity.dto.ActivityEarnResponse;
import com.techton.activity.dto.ActivityRejectRequest;
import com.techton.activity.dto.ActivityRejectResponse;
import com.techton.activity.dto.ActivitySubmitResponse;
import com.techton.activity.dto.BlogActivityRequest;
import com.techton.activity.dto.CrewActivityResponse;
import com.techton.activity.dto.PendingActivityResponse;
import com.techton.activity.dto.WeeklyActivityItemResponse;
import com.techton.activity.dto.WeeklyActivitiesResponse;
import com.techton.crew.Crew;
import com.techton.crew.CrewRepository;
import com.techton.global.BusinessException;
import com.techton.point.PointHistory;
import com.techton.point.PointHistoryRepository;
import com.techton.storage.FileStorage;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityService {

    private static final int WEEKLY_LIMIT_POINT = 100;

    private final CrewRepository crewRepository;
    private final ActivityRepository activityRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final FileStorage fileStorage;



    public int syncGithubCommit(Crew crew, LocalDate activityDate, String evidenceUrl) {
        return approveDailyActivity(crew, ActivityType.COMMIT, activityDate, evidenceUrl, "GitHub public event 자동 동기화");
    }

    public int syncGithubReview(Crew crew, LocalDate activityDate, String evidenceUrl) {
        return approveDailyActivity(crew, ActivityType.REVIEW, activityDate, evidenceUrl, "GitHub public event 자동 동기화");
    }

    public ActivitySubmitResponse mission(Long crewId, String activityDate, MultipartFile image, String memo) {
        Crew crew = findCrew(crewId);
        LocalDate date = parseDate(activityDate);
        validateFile(image);
        validateWeeklySingleSubmit(crewId, ActivityType.MISSION, date);

        String storedPath = fileStorage.store(image);
        Activity activity = activityRepository.save(new Activity(
                crew,
                ActivityType.MISSION,
                0,
                ActivityStatus.PENDING,
                storedPath,
                memo,
                date
        ));
        return new ActivitySubmitResponse(activity.getId(), activity.getStatus(), "미션 인증이 검수 대기 상태로 등록되었습니다");
    }

    public ActivitySubmitResponse blog(Long crewId, BlogActivityRequest request) {
        Crew crew = findCrew(crewId);
        LocalDate activityDate = parseDate(request.activityDate());
        validateRequired(request.blogUrl(), "블로그 URL은 필수입니다.");
        validateWeeklySingleSubmit(crewId, ActivityType.BLOG, activityDate);

        Activity activity = activityRepository.save(new Activity(
                crew,
                ActivityType.BLOG,
                0,
                ActivityStatus.PENDING,
                request.blogUrl(),
                request.memo(),
                activityDate
        ));
        return new ActivitySubmitResponse(activity.getId(), activity.getStatus(), "블로그 인증이 검수 대기 상태로 등록되었습니다");
    }

    @Transactional(readOnly = true)
    public WeeklyActivitiesResponse weeklyActivities(Long crewId) {
        findCrew(crewId);
        LocalDate today = LocalDate.now();
        LocalDate weekStartDate = weekStartDate(today);
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        int weeklyEarnedPoint = weeklyEarnedPoint(crewId, weekStartDate, weekEndDate);

        return new WeeklyActivitiesResponse(
                weekStartDate,
                weekEndDate,
                weeklyEarnedPoint,
                WEEKLY_LIMIT_POINT,
                List.of(
                        weeklyItem(crewId, ActivityType.COMMIT, "커밋", 5, 7, 35, weekStartDate, weekEndDate),
                        weeklyItem(crewId, ActivityType.REVIEW, "리뷰", 5, 7, 35, weekStartDate, weekEndDate),
                        weeklyItem(crewId, ActivityType.MISSION, "미션 성공", 10, 1, 10, weekStartDate, weekEndDate),
                        weeklyItem(crewId, ActivityType.BLOG, "회고/기술 블로그", 20, 1, 20, weekStartDate, weekEndDate)
                )
        );
    }

    @Transactional(readOnly = true)
    public List<CrewActivityResponse> getCrewActivities(Long crewId) {
        findCrew(crewId);
        return activityRepository.findByCrewIdOrderByCreatedAtDesc(crewId).stream()
                .map(CrewActivityResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PendingActivityResponse> pendingActivities() {
        return activityRepository.findByStatus(ActivityStatus.PENDING)
                .stream()
                .sorted(Comparator.comparing(Activity::getCreatedAt))
                .map(PendingActivityResponse::from)
                .toList();
    }

    public ActivityApprovalResponse approve(Long activityId) {
        Activity activity = findActivity(activityId);
        if (activity.getStatus() != ActivityStatus.PENDING) {
            throw new BusinessException("검수 대기 상태의 활동만 승인할 수 있습니다.");
        }

        int earnedPoint = calculateEarnablePoint(activity.getCrew().getId(), activity.getType(), activity.getActivityDate());
        activity.approve(earnedPoint);
        if (earnedPoint > 0) {
            activity.getCrew().earnPoint(earnedPoint);
            pointHistoryRepository.save(PointHistory.earn(
                    activity.getCrew().getId(),
                    earnedPoint,
                    activity.getType().getDisplayName() + " 인증"
            ));
        }

        return new ActivityApprovalResponse(activity.getId(), activity.getStatus(), earnedPoint);
    }

    public ActivityRejectResponse reject(Long activityId, ActivityRejectRequest request) {
        Activity activity = findActivity(activityId);
        if (activity.getStatus() != ActivityStatus.PENDING) {
            throw new BusinessException("검수 대기 상태의 활동만 거절할 수 있습니다.");
        }
        activity.reject(request.reason());
        return new ActivityRejectResponse(activity.getId(), activity.getStatus(), request.reason());
    }

    private ActivityEarnResponse approveDailyActivity(Long crewId, ActivityType type, LocalDate activityDate,
                                                     String evidenceUrl, String memo) {
        Crew crew = findCrew(crewId);
        if (hasApprovedDailyActivity(crew.getId(), type, activityDate)) {
            return new ActivityEarnResponse(type, 0, "오늘은 이미 " + type.getDisplayName() + " 포인트를 획득했습니다");
        }

        int earnedPoint = approveDailyActivity(crew, type, activityDate, evidenceUrl, memo);
        String message = earnedPoint > 0
                ? type.getDisplayName() + " 인증 완료"
                : "이번주 포인트 획득 한도에 도달했습니다";
        return new ActivityEarnResponse(type, earnedPoint, message);
    }

    private int approveDailyActivity(Crew crew, ActivityType type, LocalDate activityDate, String evidenceUrl, String memo) {
        Long crewId = crew.getId();
        if (hasApprovedDailyActivity(crewId, type, activityDate)) {
            return 0;
        }

        int earnedPoint = calculateEarnablePoint(crewId, type, activityDate);
        activityRepository.save(new Activity(crew, type, earnedPoint, ActivityStatus.APPROVED, evidenceUrl, memo, activityDate));
        if (earnedPoint > 0) {
            crew.earnPoint(earnedPoint);
            pointHistoryRepository.save(PointHistory.earn(crew.getId(), earnedPoint, type.getDisplayName() + " 인증"));
        }

        return earnedPoint;
    }

    private boolean hasApprovedDailyActivity(Long crewId, ActivityType type, LocalDate activityDate) {
        return activityRepository.existsByCrewIdAndTypeAndStatusAndActivityDate(
                crewId, type, ActivityStatus.APPROVED, activityDate);
    }

    private int calculateEarnablePoint(Long crewId, ActivityType type, LocalDate activityDate) {
        LocalDate weekStartDate = weekStartDate(activityDate);
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        int typeEarnedPoint = typeEarnedPoint(crewId, type, weekStartDate, weekEndDate);
        int weeklyEarnedPoint = weeklyEarnedPoint(crewId, weekStartDate, weekEndDate);

        int typeRemainPoint = Math.max(0, type.getWeeklyMaxPoint() - typeEarnedPoint);
        int weeklyRemainPoint = Math.max(0, WEEKLY_LIMIT_POINT - weeklyEarnedPoint);
        return Math.min(type.getPoint(), Math.min(typeRemainPoint, weeklyRemainPoint));
    }

    private WeeklyActivityItemResponse weeklyItem(Long crewId, ActivityType type, String name, int point, int maxCount,
                                                 int maxPoint, LocalDate weekStartDate, LocalDate weekEndDate) {
        int earnedCount = activityRepository.countByCrewIdAndTypeAndStatusAndActivityDateBetween(
                crewId, type, ActivityStatus.APPROVED, weekStartDate, weekEndDate);
        int earnedPoint = typeEarnedPoint(crewId, type, weekStartDate, weekEndDate);
        return new WeeklyActivityItemResponse(type, name, point, earnedCount, maxCount, earnedPoint, maxPoint);
    }

    private int typeEarnedPoint(Long crewId, ActivityType type, LocalDate weekStartDate, LocalDate weekEndDate) {
        return activityRepository.findByCrewIdAndStatusAndActivityDateBetween(
                        crewId, ActivityStatus.APPROVED, weekStartDate, weekEndDate)
                .stream()
                .filter(activity -> activity.getType() == type)
                .mapToInt(Activity::getPoint)
                .sum();
    }

    private int weeklyEarnedPoint(Long crewId, LocalDate weekStartDate, LocalDate weekEndDate) {
        return activityRepository.sumPointByCrewIdAndStatusAndActivityDateBetween(
                crewId, ActivityStatus.APPROVED, weekStartDate, weekEndDate);
    }

    private void validateWeeklySingleSubmit(Long crewId, ActivityType type, LocalDate activityDate) {
        LocalDate weekStartDate = weekStartDate(activityDate);
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        if (activityRepository.existsByCrewIdAndTypeAndStatusInAndActivityDateBetween(
                crewId,
                type,
                List.of(ActivityStatus.APPROVED, ActivityStatus.PENDING),
                weekStartDate,
                weekEndDate
        )) {
            throw new BusinessException("이번주 " + type.getDisplayName() + " 인증은 이미 등록되었습니다.");
        }
    }

    private Crew findCrew(Long crewId) {
        return crewRepository.findById(crewId)
                .orElseThrow(() -> new BusinessException("크루를 찾을 수 없습니다."));
    }

    private Activity findActivity(Long activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException("활동을 찾을 수 없습니다."));
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (RuntimeException exception) {
            throw new BusinessException("활동 일자는 yyyy-MM-dd 형식이어야 합니다.");
        }
    }

    private LocalDate weekStartDate(LocalDate date) {
        return date.with(DayOfWeek.MONDAY);
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(message);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("미션 인증 사진은 필수입니다.");
        }
    }
}
