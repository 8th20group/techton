package com.techton.crew;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrewRepository extends JpaRepository<Crew, Long> {

    boolean existsByGithubId(String githubId);

    Optional<Crew> findByGithubId(String githubId);
}
