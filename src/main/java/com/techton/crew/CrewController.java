package com.techton.crew;

import com.techton.crew.dto.CrewCreateRequest;
import com.techton.crew.dto.CrewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CrewController {

    private final CrewService crewService;

    @PostMapping("/crews")
    public CrewResponse create(@RequestBody CrewCreateRequest request) {
        return crewService.create(request);
    }
}
