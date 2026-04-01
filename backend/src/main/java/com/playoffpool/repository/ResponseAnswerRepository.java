package com.playoffpool.repository;

import com.playoffpool.model.ResponseAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponseAnswerRepository extends JpaRepository<ResponseAnswer, Integer> {
    List<ResponseAnswer> findByResponseId(Integer responseId);

    List<ResponseAnswer> findByResponseIdIn(List<Integer> responseIds);

    List<ResponseAnswer> findByQuestionId(Integer questionId);
}
