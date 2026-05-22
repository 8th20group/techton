package com.techton.github;

import com.techton.activity.ActivityService;
import com.techton.crew.Crew;
import com.techton.crew.CrewRepository;
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

        return new GitHubActivitySyncResponse(activityDate, crews.size(), commitSyncedCount, reviewSyncedCount);
    }
}
