package com.techton.shop;

import com.techton.shop.dto.CoachTicketRequest;
import com.techton.shop.dto.CoachTicketResponse;
import com.techton.shop.dto.RandomBoxResponse;
import com.techton.shop.dto.ShopItemResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @GetMapping("/shop/items")
    public List<ShopItemResponse> findItems() {
        return shopService.findItems();
    }

    @PostMapping("/crews/{crewId}/shop/random-box")
    public RandomBoxResponse buyRandomBox(@PathVariable Long crewId) {
        return shopService.buyRandomBox(crewId);
    }

    @PostMapping("/crews/{crewId}/shop/coach-tickets")
    public CoachTicketResponse buyCoachTicket(
            @PathVariable Long crewId,
            @RequestBody CoachTicketRequest request
    ) {
        return shopService.buyCoachTicket(crewId, request);
    }
}
