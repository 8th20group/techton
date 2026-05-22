package com.techton.activity.dto;

public record CommitActivityRequest(
        String activityDate,
        String githubUrl
) {
}
