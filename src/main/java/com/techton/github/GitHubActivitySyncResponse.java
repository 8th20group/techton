package com.techton.github;

import java.time.LocalDate;

public record GitHubActivitySyncResponse(
        LocalDate activityDate,
        int crewCount,
        int commitSyncedCount,
        int reviewSyncedCount
) {
}
