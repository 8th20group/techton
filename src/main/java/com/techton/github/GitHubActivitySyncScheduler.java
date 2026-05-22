package com.techton.github;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GitHubActivitySyncScheduler {

    private final GitHubActivitySyncService gitHubActivitySyncService;

    @Scheduled(cron = "0 0 6 * * *", zone = "Asia/Seoul")
    public void syncYesterday() {
        gitHubActivitySyncService.sync(LocalDate.now().minusDays(1));
    }
}
