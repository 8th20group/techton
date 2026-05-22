package com.techton.coach;

import com.techton.coach.dto.CoachResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CoachController {

    private final CoachService coachService;

    @GetMapping("/coaches")
    public List<CoachResponse> findAll() {
        return coachService.findAll();
    }
}
