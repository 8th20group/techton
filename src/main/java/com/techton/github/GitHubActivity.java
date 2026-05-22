package com.techton.github;

public record GitHubActivity(
        boolean committed,
        String commitUrl,
        boolean reviewed,
        String reviewUrl
) {
}
