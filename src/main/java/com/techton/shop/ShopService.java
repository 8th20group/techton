package com.techton.shop;

import com.techton.coach.Coach;
import com.techton.coach.CoachService;
import com.techton.crew.Crew;
import com.techton.crew.CrewRepository;
import com.techton.global.BusinessException;
import com.techton.global.WeekRange;
import com.techton.point.PointHistory;
import com.techton.point.PointHistoryRepository;
import com.techton.shop.dto.CoachTicketRequest;
import com.techton.shop.dto.CoachTicketResponse;
import com.techton.shop.dto.RandomBoxResponse;
import com.techton.shop.dto.ShopItemResponse;
import com.techton.ticket.Ticket;
import com.techton.ticket.TicketRepository;
import com.techton.ticket.TicketType;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ShopService {

    private static final int RANDOM_BOX_PRICE = ShopItemType.RANDOM_BOX.getPrice();

    private final CrewRepository crewRepository;
    private final TicketRepository ticketRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final CoachService coachService;

    @Transactional(readOnly = true)
    public List<ShopItemResponse> findItems() {
        return Arrays.stream(ShopItemType.values())
                .map(ShopItemResponse::from)
                .toList();
    }

    public RandomBoxResponse buyRandomBox(Long crewId) {
        Crew crew = findCrew(crewId);

        // 13. 구현 메모: 먼저 10P를 차감한다. (Crew.version 으로 동시 구매 시 낙관적 락 충돌 처리)
        crew.usePoint(RANDOM_BOX_PRICE);
        pointHistoryRepository.save(PointHistory.use(crewId, RANDOM_BOX_PRICE, "랜덤 박스 구매"));

        RandomBoxReward reward = RandomBoxReward.draw();
        if (reward.isCoachTicket()) {
            Coach coach = coachService.assignRandomCoach();
            ticketRepository.save(new Ticket(crewId, coach.getId(), TicketType.CAFE));
            return RandomBoxResponse.coachTicket(coach, crew.getPoint());
        }
        if (reward.isLose()) {
            return RandomBoxResponse.lose(crew.getPoint());
        }

        int rewardPoint = reward.getRewardPoint();
        crew.earnPoint(rewardPoint);
        pointHistoryRepository.save(PointHistory.earn(crewId, rewardPoint, "랜덤 박스 당첨"));
        return RandomBoxResponse.point(rewardPoint, crew.getPoint());
    }

    public CoachTicketResponse buyCoachTicket(Long crewId, CoachTicketRequest request) {
        if (request.ticketType() == null) {
            throw new BusinessException("이용권 종류는 필수입니다.");
        }
        Crew crew = findCrew(crewId);
        TicketType type = request.ticketType();

        validateWeeklyLimit(crewId, type);

        // 13. 구현 메모: 먼저 포인트를 차감하고 랜덤 코치를 배정한다.
        crew.usePoint(type.getPrice());
        pointHistoryRepository.save(PointHistory.use(crewId, type.getPrice(), "코치 이용권 구매"));

        Coach coach = coachService.assignRandomCoach();
        Ticket ticket = ticketRepository.save(new Ticket(crewId, coach.getId(), type));
        return CoachTicketResponse.of(ticket, coach, crew.getPoint());
    }

    private void validateWeeklyLimit(Long crewId, TicketType type) {
        boolean alreadyBought = ticketRepository.existsByCrewIdAndTypeAndCreatedAtBetween(
                crewId, type, WeekRange.startOfWeek(), WeekRange.endOfWeek());
        if (alreadyBought) {
            throw new BusinessException("이번 주에 이미 구매한 이용권입니다.");
        }
    }

    private Crew findCrew(Long crewId) {
        return crewRepository.findById(crewId)
                .orElseThrow(() -> new BusinessException("크루를 찾을 수 없습니다."));
    }
}
