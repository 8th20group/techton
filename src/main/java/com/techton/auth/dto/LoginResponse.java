package com.techton.auth.dto;

public record LoginResponse(
        Long crewId,
        String githubId,
        String nickname
) {
}
