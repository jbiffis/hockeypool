package com.playoffpool.repository;

import com.playoffpool.model.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Integer> {
    List<QuestionOption> findByQuestionIdOrderByDisplayOrder(Integer questionId);

    List<QuestionOption> findByQuestionIdInOrderByDisplayOrder(List<Integer> questionIds);
}
