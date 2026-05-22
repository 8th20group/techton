package com.techton.coach;

import com.techton.coach.dto.CoachResponse;
import com.techton.global.BusinessException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoachService {

    private final CoachRepository coachRepository;

    public List<CoachResponse> findAll() {
        return coachRepository.findAll().stream()
                .map(CoachResponse::from)
                .toList();
    }

    public Coach assignRandomCoach() {
        List<Coach> coaches = coachRepository.findAll();
        if (coaches.isEmpty()) {
            throw new BusinessException("배정 가능한 코치가 없습니다.");
        }
        return coaches.get(ThreadLocalRandom.current().nextInt(coaches.size()));
    }

    public Coach findById(Long coachId) {
        return coachRepository.findById(coachId)
                .orElseThrow(() -> new BusinessException("코치를 찾을 수 없습니다."));
    }
}
