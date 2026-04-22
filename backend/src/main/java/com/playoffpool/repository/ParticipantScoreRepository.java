package com.playoffpool.repository;

import com.playoffpool.model.ParticipantScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ParticipantScoreRepository extends JpaRepository<ParticipantScore, Integer> {
    List<ParticipantScore> findByParticipantId(Integer participantId);
    List<ParticipantScore> findByParticipantIdAndRoundId(Integer participantId, Integer roundId);
    List<ParticipantScore> findByRoundId(Integer roundId);
    List<ParticipantScore> findByQuestionId(Integer questionId);

    @Query(value = "SELECT MAX(updated_at) FROM participant_scores ps "
            + "JOIN participants p ON p.id = ps.participant_id "
            + "WHERE p.season_id = :seasonId", nativeQuery = true)
    Instant findMaxUpdatedAtForSeason(Integer seasonId);
}
