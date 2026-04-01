package com.playoffpool.repository;

import com.playoffpool.model.Response;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResponseRepository extends JpaRepository<Response, Integer> {
    Optional<Response> findByParticipantIdAndRoundId(Integer participantId, Integer roundId);

    List<Response> findByRoundId(Integer roundId);

    List<Response> findByParticipantId(Integer participantId);
}
