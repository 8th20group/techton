package com.techton.ranking;

import com.techton.ranking.dto.CoachRankingResponse;
import com.techton.ranking.dto.CrewRankingResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/rankings/crews")
    public List<CrewRankingResponse> crewRankings() {
        return rankingService.crewRankings();
    }

    @GetMapping("/rankings/coaches")
    public List<CoachRankingResponse> coachRankings() {
        return rankingService.coachRankings();
    }
}
