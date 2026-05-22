package com.techton.github;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class PublicGitHubActivityClient implements GitHubActivityClient {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
    private static final String PUSH_EVENT = "PushEvent";
    private static final String REVIEW_EVENT = "PullRequestReviewEvent";
    private static final String REVIEW_COMMENT_EVENT = "PullRequestReviewCommentEvent";

    private final RestClient restClient;

    public PublicGitHubActivityClient(
            RestClient.Builder restClientBuilder,
            @Value("${github.api.base-url:https://api.github.com}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public GitHubActivity findDailyActivity(String githubId, LocalDate activityDate) {
        try {
            JsonNode events = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/users/{githubId}/events/public")
                            .queryParam("per_page", 100)
                            .build(githubId))
                    .retrieve()
                    .body(JsonNode.class);
            return parse(events, activityDate);
        } catch (RestClientException exception) {
            return new GitHubActivity(false, null, false, null);
        }
    }

    private GitHubActivity parse(JsonNode events, LocalDate activityDate) {
        boolean committed = false;
        boolean reviewed = false;
        String commitUrl = null;
        String reviewUrl = null;

        if (events == null || !events.isArray()) {
            return new GitHubActivity(false, null, false, null);
        }

        for (JsonNode event : events) {
            if (!isTargetDate(event, activityDate)) {
                continue;
            }

            String type = event.path("type").asText();
            if (!committed && PUSH_EVENT.equals(type)) {
                committed = true;
                commitUrl = extractCommitUrl(event).orElse(event.path("repo").path("url").asText(null));
            }
            if (!reviewed && (REVIEW_EVENT.equals(type) || REVIEW_COMMENT_EVENT.equals(type))) {
                reviewed = true;
                reviewUrl = extractReviewUrl(event).orElse(event.path("repo").path("url").asText(null));
            }
            if (committed && reviewed) {
                break;
            }
        }

        return new GitHubActivity(committed, commitUrl, reviewed, reviewUrl);
    }

    private boolean isTargetDate(JsonNode event, LocalDate activityDate) {
        String createdAt = event.path("created_at").asText(null);
        if (createdAt == null) {
            return false;
        }
        LocalDate eventDate = Instant.parse(createdAt).atZone(SERVICE_ZONE).toLocalDate();
        return activityDate.equals(eventDate);
    }

    private Optional<String> extractCommitUrl(JsonNode event) {
        JsonNode commits = event.path("payload").path("commits");
        if (!commits.isArray() || commits.isEmpty()) {
            return Optional.empty();
        }
        String url = commits.get(0).path("url").asText(null);
        if (url == null) {
            return Optional.empty();
        }
        return Optional.of(url.replace("api.github.com/repos", "github.com").replace("/commits/", "/commit/"));
    }

    private Optional<String> extractReviewUrl(JsonNode event) {
        String reviewUrl = event.path("payload").path("review").path("html_url").asText(null);
        if (reviewUrl != null) {
            return Optional.of(reviewUrl);
        }
        String commentUrl = event.path("payload").path("comment").path("html_url").asText(null);
        return Optional.ofNullable(commentUrl);
    }
}
