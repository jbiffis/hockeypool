package com.playoffpool.repository;

import com.playoffpool.model.ParticipantScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipantScoreRepository extends JpaRepository<ParticipantScore, Integer> {
    List<ParticipantScore> findByParticipantId(Integer participantId);
    List<ParticipantScore> findByParticipantIdAndRoundId(Integer participantId, Integer roundId);
    List<ParticipantScore> findByRoundId(Integer roundId);
    List<ParticipantScore> findByQuestionId(Integer questionId);
}
