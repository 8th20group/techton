package com.techton.coach;

import com.techton.crew.Track;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoachDataInitializer implements CommandLineRunner {

    private final CoachRepository coachRepository;

    @Override
    public void run(String... args) {
        if (coachRepository.count() > 0) {
            return;
        }
        coachRepository.saveAll(List.of(
                new Coach("브리", Track.BE),
                new Coach("검프", Track.BE),
                new Coach("네오", Track.BE),
                new Coach("구구", Track.BE),
                new Coach("브라운", Track.BE),
                new Coach("워니", Track.SOFT),
                new Coach("류시", Track.SOFT),
                new Coach("리사", Track.SOFT),
                new Coach("준", Track.FE),
                new Coach("시지프", Track.FE),
                new Coach("제임스", Track.ANDROID),
                new Coach("디노", Track.ANDROID)
        ));
    }
}
