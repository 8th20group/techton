package com.techton.github;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GitHubActivitySyncController {

    private final GitHubActivitySyncService gitHubActivitySyncService;

    @PostMapping("/admin/activities/github/sync")
    public GitHubActivitySyncResponse sync(@RequestParam(required = false) LocalDate activityDate) {
        LocalDate targetDate = activityDate == null ? LocalDate.now().minusDays(1) : activityDate;
        return gitHubActivitySyncService.sync(targetDate);
    }
}
