package com.techton.github;

import com.techton.activity.ActivityService;
import com.techton.crew.Crew;
import com.techton.crew.CrewRepository;
import com.techton.global.BusinessException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GitHubActivitySyncService {

    private final CrewRepository crewRepository;
    private final GitHubActivityClient gitHubActivityClient;
    private final ActivityService activityService;

    @Transactional
    public GitHubActivitySyncResponse sync(LocalDate activityDate) {
        List<Crew> crews = crewRepository.findAll();
        SyncResult result = sync(crews, activityDate);
        return new GitHubActivitySyncResponse(
                activityDate,
                crews.size(),
                result.commitSyncedCount(),
                result.reviewSyncedCount(),
                "전체 크루 GitHub 활동 동기화 완료"
        );
    }

    @Transactional
    public GitHubActivitySyncResponse syncCrew(Long crewId, LocalDate activityDate) {
        Crew crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new BusinessException("크루를 찾을 수 없습니다."));
        SyncResult result = sync(List.of(crew), activityDate);
        return new GitHubActivitySyncResponse(
                activityDate,
                1,
                result.commitSyncedCount(),
                result.reviewSyncedCount(),
                "내 GitHub 활동 동기화 완료"
        );
    }

    private SyncResult sync(List<Crew> crews, LocalDate activityDate) {
        int commitSyncedCount = 0;
        int reviewSyncedCount = 0;

        for (Crew crew : crews) {
            GitHubActivity activity = gitHubActivityClient.findDailyActivity(crew.getGithubId(), activityDate);
            if (activity.committed()) {
                int earnedPoint = activityService.syncGithubCommit(crew, activityDate, activity.commitUrl());
                if (earnedPoint > 0) {
                    commitSyncedCount++;
                }
            }
            if (activity.reviewed()) {
                int earnedPoint = activityService.syncGithubReview(crew, activityDate, activity.reviewUrl());
                if (earnedPoint > 0) {
                    reviewSyncedCount++;
                }
            }
        }

        return new SyncResult(commitSyncedCount, reviewSyncedCount);
    }

    private record SyncResult(
            int commitSyncedCount,
            int reviewSyncedCount
    ) {
    }
}
