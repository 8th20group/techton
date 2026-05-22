package com.techton.crew.dto;

import com.techton.crew.Track;

public record CrewCreateRequest(
        String githubId,
        String nickname,
        Track track
) {
}
