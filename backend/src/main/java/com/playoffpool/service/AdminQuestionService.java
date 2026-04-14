package com.playoffpool.service;

import com.playoffpool.dto.QuestionDto;
import com.playoffpool.dto.QuestionOptionDto;
import com.playoffpool.model.Question;
import com.playoffpool.model.QuestionOption;
import com.playoffpool.model.Round;
import com.playoffpool.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class AdminQuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final RoundRepository roundRepository;
    private final ResponseAnswerRepository responseAnswerRepository;
    private final ParticipantScoreRepository participantScoreRepository;

    public AdminQuestionService(QuestionRepository questionRepository,
                                QuestionOptionRepository questionOptionRepository,
                                RoundRepository roundRepository,
                                ResponseAnswerRepository responseAnswerRepository,
                                ParticipantScoreRepository participantScoreRepository) {
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.roundRepository = roundRepository;
        this.responseAnswerRepository = responseAnswerRepository;
        this.participantScoreRepository = participantScoreRepository;
    }

    public List<QuestionDto> getQuestionsByRound(Integer roundId) {
        return questionRepository.findByRoundIdOrderByDisplayOrder(roundId)
                .stream()
                .map(QuestionDto::fromEntity)
                .collect(Collectors.toList());
    }

    public QuestionDto getQuestion(Integer id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Question not found with id: " + id));
        return QuestionDto.fromEntity(question);
    }

    public QuestionDto createQuestion(Integer roundId, QuestionDto dto) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new NoSuchElementException("Round not found with id: " + roundId));

        Question question = new Question();
        question.setRound(round);
        question.setTitle(dto.getTitle());
        question.setDescription(dto.getDescription());
        question.setImageUrl(dto.getImageUrl());
        question.setQuestionType(dto.getQuestionType());
        question.setIsMandatory(dto.getIsMandatory());
        question.setDisplayOrder(dto.getDisplayOrder());
        question.setMaxWager(dto.getMaxWager());
        question.setMaxSelections(dto.getMaxSelections());
        question.setCreatedAt(LocalDateTime.now());

        if (dto.getParentQuestionId() != null) {
            Question parent = questionRepository.findById(dto.getParentQuestionId())
                    .orElseThrow(() -> new NoSuchElementException("Parent question not found with id: " + dto.getParentQuestionId()));
            question.setParentQuestion(parent);
        }

        Question saved = questionRepository.save(question);
        return QuestionDto.fromEntity(saved);
    }

    public QuestionDto updateQuestion(Integer id, QuestionDto dto) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Question not found with id: " + id));

        question.setTitle(dto.getTitle());
        question.setDescription(dto.getDescription());
        question.setImageUrl(dto.getImageUrl());
        question.setQuestionType(dto.getQuestionType());
        question.setIsMandatory(dto.getIsMandatory());
        question.setDisplayOrder(dto.getDisplayOrder());
        question.setMaxWager(dto.getMaxWager());
        question.setMaxSelections(dto.getMaxSelections());

        if (dto.getParentQuestionId() != null) {
            Question parent = questionRepository.findById(dto.getParentQuestionId())
                    .orElseThrow(() -> new NoSuchElementException("Parent question not found with id: " + dto.getParentQuestionId()));
            question.setParentQuestion(parent);
        } else {
            question.setParentQuestion(null);
        }

        Question saved = questionRepository.save(question);
        return QuestionDto.fromEntity(saved);
    }

    @Transactional
    public void deleteQuestion(Integer id) {
        participantScoreRepository.deleteAll(participantScoreRepository.findByQuestionId(id));
        responseAnswerRepository.deleteAll(responseAnswerRepository.findByQuestionId(id));
        questionOptionRepository.deleteAll(questionOptionRepository.findByQuestionIdOrderByDisplayOrder(id));
        questionRepository.deleteById(id);
    }

    public List<QuestionOptionDto> getOptionsByQuestion(Integer questionId) {
        return questionOptionRepository.findByQuestionIdOrderByDisplayOrder(questionId)
                .stream()
                .map(QuestionOptionDto::fromEntity)
                .collect(Collectors.toList());
    }

    public QuestionOptionDto createOption(Integer questionId, QuestionOptionDto dto) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NoSuchElementException("Question not found with id: " + questionId));

        QuestionOption option = new QuestionOption();
        option.setQuestion(question);
        option.setOptionText(dto.getOptionText());
        option.setDisplayOrder(dto.getDisplayOrder());
        option.setPoints(dto.getPoints());
        option.setSubtext(dto.getSubtext());
        option.setImageUrl(dto.getImageUrl());

        QuestionOption saved = questionOptionRepository.save(option);
        return QuestionOptionDto.fromEntity(saved);
    }

    public QuestionOptionDto updateOption(Integer optionId, QuestionOptionDto dto) {
        QuestionOption option = questionOptionRepository.findById(optionId)
                .orElseThrow(() -> new NoSuchElementException("Option not found with id: " + optionId));

        option.setOptionText(dto.getOptionText());
        option.setDisplayOrder(dto.getDisplayOrder());
        option.setPoints(dto.getPoints());
        option.setSubtext(dto.getSubtext());
        option.setImageUrl(dto.getImageUrl());

        QuestionOption saved = questionOptionRepository.save(option);
        return QuestionOptionDto.fromEntity(saved);
    }

    public void deleteOption(Integer optionId) {
        questionOptionRepository.deleteById(optionId);
    }

    public QuestionOptionDto updateOptionPoints(Integer optionId, Integer points) {
        QuestionOption option = questionOptionRepository.findById(optionId)
                .orElseThrow(() -> new NoSuchElementException("Option not found with id: " + optionId));

        option.setPoints(points);

        QuestionOption saved = questionOptionRepository.save(option);
        return QuestionOptionDto.fromEntity(saved);
    }
}
