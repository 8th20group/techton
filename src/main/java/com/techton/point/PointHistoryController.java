package com.techton.point;

import com.techton.point.dto.PointHistoryResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PointHistoryController {

    private final PointHistoryService pointHistoryService;

    @GetMapping("/crews/{crewId}/point-histories")
    public List<PointHistoryResponse> findByCrew(@PathVariable Long crewId) {
        return pointHistoryService.findByCrew(crewId);
    }
}
