package com.techton.crew;

import com.techton.crew.dto.CrewCreateRequest;
import com.techton.crew.dto.CrewResponse;
import com.techton.global.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CrewService {

    private final CrewRepository crewRepository;

    public CrewResponse create(CrewCreateRequest request) {
        validate(request);
        if (crewRepository.existsByGithubId(request.githubId())) {
            throw new BusinessException("이미 가입된 GitHub ID입니다.");
        }

        Crew crew = crewRepository.save(new Crew(request.githubId(), request.nickname(), request.track()));
        return CrewResponse.from(crew);
    }

    private void validate(CrewCreateRequest request) {
        if (request.githubId() == null || request.githubId().isBlank()) {
            throw new BusinessException("GitHub ID는 필수입니다.");
        }
        if (request.nickname() == null || request.nickname().isBlank()) {
            throw new BusinessException("닉네임은 필수입니다.");
        }
        if (request.track() == null) {
            throw new BusinessException("분야는 필수입니다.");
        }
    }
}
