package com.techton.point;

import com.techton.point.dto.PointSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @GetMapping("/crews/{crewId}/points/summary")
    public PointSummaryResponse summary(@PathVariable Long crewId) {
        return pointService.summary(crewId);
    }
}
