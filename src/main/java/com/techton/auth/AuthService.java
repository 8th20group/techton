package com.techton.auth;

import com.techton.auth.dto.LoginRequest;
import com.techton.auth.dto.LoginResponse;
import com.techton.auth.dto.MeResponse;
import com.techton.crew.Crew;
import com.techton.crew.CrewRepository;
import com.techton.global.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final CrewRepository crewRepository;

    public LoginResponse login(LoginRequest request) {
        validateGithubId(request.githubId());
        Crew crew = findCrew(request.githubId());
        return new LoginResponse(crew.getId(), crew.getGithubId(), crew.getNickname());
    }

    public MeResponse me(String githubId) {
        validateGithubId(githubId);
        Crew crew = findCrew(githubId);
        return new MeResponse(
                crew.getId(),
                crew.getGithubId(),
                crew.getNickname(),
                crew.getGeneration(),
                crew.getTrack(),
                crew.getPoint()
        );
    }

    private Crew findCrew(String githubId) {
        return crewRepository.findByGithubId(githubId)
                .orElseThrow(() -> new BusinessException("가입되지 않은 GitHub ID입니다."));
    }

    private void validateGithubId(String githubId) {
        if (githubId == null || githubId.isBlank()) {
            throw new BusinessException("GitHub ID는 필수입니다.");
        }
    }
}
