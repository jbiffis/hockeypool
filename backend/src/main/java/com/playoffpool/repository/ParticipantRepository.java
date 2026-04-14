package com.playoffpool.repository;

import com.playoffpool.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Integer> {
    Optional<Participant> findByEmail(String email);
    Optional<Participant> findByEmailAndSeasonId(String email, Integer seasonId);
    List<Participant> findBySeasonId(Integer seasonId);
    long countBySeasonId(Integer seasonId);
}
