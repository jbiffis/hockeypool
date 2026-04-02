package com.playoffpool.repository;

import com.playoffpool.model.Division;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DivisionRepository extends JpaRepository<Division, Integer> {

    List<Division> findBySeasonIdOrderByNameAsc(Integer seasonId);

    @Query("SELECT d FROM Division d JOIN d.participants p WHERE p.id = :participantId AND d.season.id = :seasonId ORDER BY d.name ASC")
    List<Division> findByParticipantIdAndSeasonId(@Param("participantId") Integer participantId,
                                                   @Param("seasonId") Integer seasonId);
}
