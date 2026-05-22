package com.techton.crew;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrewRepository extends JpaRepository<Crew, Long> {

    Optional<Crew> findByGithubIdIgnoreCase(String githubId);

    List<Crew> findAllByOrderByPointDescIdAsc();
}
