package com.techton.point;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    List<PointHistory> findByCrewIdOrderByCreatedAtDesc(Long crewId);
}
