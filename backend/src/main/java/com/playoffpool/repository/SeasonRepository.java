package com.playoffpool.repository;

import com.playoffpool.model.Season;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeasonRepository extends JpaRepository<Season, Integer> {
    List<Season> findAllByOrderByYearDesc();
    Optional<Season> findByStatus(String status);
}
