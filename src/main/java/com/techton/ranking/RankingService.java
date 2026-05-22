package com.techton.ranking;

import com.techton.coach.Coach;
import com.techton.coach.CoachRepository;
import com.techton.crew.Crew;
import com.techton.crew.CrewRepository;
import com.techton.global.WeekRange;
import com.techton.ranking.dto.CoachRankingResponse;
import com.techton.ranking.dto.CrewRankingResponse;
import com.techton.ticket.TicketRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService {

    private final CrewRepository crewRepository;
    private final CoachRepository coachRepository;
    private final TicketRepository ticketRepository;

    public List<CrewRankingResponse> crewRankings() {
        List<Crew> crews = crewRepository.findAllByOrderByPointDescIdAsc();
        return IntStream.range(0, crews.size())
                .mapToObj(index -> CrewRankingResponse.of(index + 1, crews.get(index)))
                .toList();
    }

    public List<CoachRankingResponse> coachRankings() {
        Map<Long, Coach> coaches = coachRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Coach::getId, Function.identity()));

        List<CoachRankingResponse> rankings = ticketRepository
                .countUsedTicketsGroupByCoachId(WeekRange.startOfWeek(), WeekRange.endOfWeek())
                .stream()
                .map(row -> toCoachRanking(row, coaches))
                .filter(response -> response.coachName() != null)
                .sorted(Comparator
                        .comparingLong(CoachRankingResponse::usedCount).reversed()
                        .thenComparing(CoachRankingResponse::coachName))
                .toList();

        return IntStream.range(0, rankings.size())
                .mapToObj(index -> rankings.get(index).withRank(index + 1))
                .toList();
    }

    private CoachRankingResponse toCoachRanking(Object[] row, Map<Long, Coach> coaches) {
        Long coachId = (Long) row[0];
        long usedCount = (Long) row[1];
        Coach coach = coaches.get(coachId);
        if (coach == null) {
            return new CoachRankingResponse(0, null, usedCount);
        }
        return new CoachRankingResponse(0, coach.getName(), usedCount);
    }
}
