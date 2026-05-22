package com.techton.github;

import java.time.LocalDate;

public interface GitHubActivityClient {

    GitHubActivity findDailyActivity(String githubId, LocalDate activityDate);
}
