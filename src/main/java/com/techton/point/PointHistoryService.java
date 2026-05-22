package com.techton.point;

import com.techton.point.dto.PointHistoryResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointHistoryService {

    private final PointHistoryRepository pointHistoryRepository;

    public List<PointHistoryResponse> findByCrew(Long crewId) {
        return pointHistoryRepository.findByCrewIdOrderByCreatedAtDesc(crewId).stream()
                .map(PointHistoryResponse::from)
                .toList();
    }
}
