package com.playoffpool.repository;

import com.playoffpool.model.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoundRepository extends JpaRepository<Round, Integer> {
    List<Round> findAllByOrderByDisplayOrderAsc();

    List<Round> findBySeasonIdOrderByDisplayOrderAsc(Integer seasonId);

    List<Round> findByStatus(String status);

    List<Round> findBySeasonIdAndStatus(Integer seasonId, String status);

    List<Round> findByDisplayWithRoundId(Integer displayWithRoundId);
}
