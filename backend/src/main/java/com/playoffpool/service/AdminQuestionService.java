package com.playoffpool.service;

import com.playoffpool.dto.QuestionDto;
import com.playoffpool.dto.QuestionOptionDto;
import com.playoffpool.model.Participant;
import com.playoffpool.model.ParticipantScore;
import com.playoffpool.model.Question;
import com.playoffpool.model.QuestionOption;
import com.playoffpool.model.Response;
import com.playoffpool.model.ResponseAnswer;
import com.playoffpool.model.Round;
import com.playoffpool.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminQuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final RoundRepository roundRepository;
    private final ResponseAnswerRepository responseAnswerRepository;
    private final ParticipantScoreRepository participantScoreRepository;
    private final ParticipantRepository participantRepository;

    public AdminQuestionService(QuestionRepository questionRepository,
                                QuestionOptionRepository questionOptionRepository,
                                RoundRepository roundRepository,
                                ResponseAnswerRepository responseAnswerRepository,
                                ParticipantScoreRepository participantScoreRepository,
                                ParticipantRepository participantRepository) {
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.roundRepository = roundRepository;
        this.responseAnswerRepository = responseAnswerRepository;
        this.participantScoreRepository = participantScoreRepository;
        this.participantRepository = participantRepository;
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

    @Transactional
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
        question.setPoints(dto.getPoints());
        question.setCreatedAt(LocalDateTime.now());

        if (dto.getParentQuestionId() != null) {
            Question parent = questionRepository.findById(dto.getParentQuestionId())
                    .orElseThrow(() -> new NoSuchElementException("Parent question not found with id: " + dto.getParentQuestionId()));
            question.setParentQuestion(parent);
        }

        Question saved = questionRepository.save(question);

        if ("best_team_name".equals(saved.getQuestionType()) && round.getSeason() != null) {
            generateBestTeamNameOptions(saved, round.getSeason().getId());
        }

        return QuestionDto.fromEntity(saved);
    }

    private void generateBestTeamNameOptions(Question question, Integer seasonId) {
        List<Participant> participants = participantRepository.findBySeasonId(seasonId).stream()
                .filter(p -> p.getTeamName() != null && !p.getTeamName().trim().isEmpty())
                .sorted(Comparator.comparing(p -> p.getTeamName().toLowerCase()))
                .collect(Collectors.toList());

        int order = 1;
        for (Participant p : participants) {
            QuestionOption option = new QuestionOption();
            option.setQuestion(question);
            String label = p.getTeamName().trim();
            if (p.getName() != null && !p.getName().trim().isEmpty()) {
                label = label + " - " + p.getName().trim();
            }
            option.setOptionText(label);
            option.setDisplayOrder(order++);
            option.setParticipantId(p.getId());
            questionOptionRepository.save(option);
        }
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
        question.setPoints(dto.getPoints());

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
        option.setBoxGroup(dto.getBoxGroup());

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
        option.setBoxGroup(dto.getBoxGroup());

        QuestionOption saved = questionOptionRepository.save(option);
        return QuestionOptionDto.fromEntity(saved);
    }

    public void deleteOption(Integer optionId) {
        questionOptionRepository.deleteById(optionId);
    }

    @Transactional
    public Map<String, Object> scoreBestTeamName(Integer questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NoSuchElementException("Question not found with id: " + questionId));
        if (!"best_team_name".equals(question.getQuestionType())) {
            throw new IllegalArgumentException("Question is not a best_team_name question");
        }
        int pointsValue = question.getPoints() != null ? question.getPoints() : 0;
        Round round = question.getRound();

        List<ResponseAnswer> answers = responseAnswerRepository.findByQuestionId(questionId);

        Map<Integer, Integer> votesByOption = new HashMap<>();
        Map<Integer, List<Integer>> pickersByOption = new HashMap<>();
        for (ResponseAnswer ra : answers) {
            QuestionOption opt = ra.getSelectedOption();
            if (opt == null) continue;
            Integer optId = opt.getId();
            votesByOption.merge(optId, 1, Integer::sum);
            Integer pid = ra.getResponse() != null && ra.getResponse().getParticipant() != null
                    ? ra.getResponse().getParticipant().getId()
                    : null;
            if (pid != null) {
                pickersByOption.computeIfAbsent(optId, k -> new ArrayList<>()).add(pid);
            }
        }

        int max = votesByOption.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        Set<Integer> winningOptionIds = new HashSet<>();
        if (max > 0) {
            for (Map.Entry<Integer, Integer> e : votesByOption.entrySet()) {
                if (e.getValue() == max) winningOptionIds.add(e.getKey());
            }
        }

        Set<Integer> winningPickers = new HashSet<>();
        for (Integer optId : winningOptionIds) {
            winningPickers.addAll(pickersByOption.getOrDefault(optId, List.of()));
        }

        List<QuestionOption> allOpts = questionOptionRepository.findByQuestionIdOrderByDisplayOrder(questionId);
        for (QuestionOption opt : allOpts) {
            opt.setPoints(winningOptionIds.contains(opt.getId()) ? pointsValue : 0);
            questionOptionRepository.save(opt);
        }

        List<String> winnerLabels = allOpts.stream()
                .filter(o -> winningOptionIds.contains(o.getId()))
                .map(QuestionOption::getOptionText)
                .collect(Collectors.toList());
        question.setCorrectAnswerText(String.join(" | ", winnerLabels));
        questionRepository.save(question);

        List<ParticipantScore> existing = participantScoreRepository.findByQuestionId(questionId);
        Map<Integer, ParticipantScore> existingByPid = new HashMap<>();
        for (ParticipantScore ps : existing) {
            if (ps.getParticipant() != null) {
                existingByPid.put(ps.getParticipant().getId(), ps);
            }
        }

        for (ResponseAnswer ra : answers) {
            Participant p = ra.getResponse() != null ? ra.getResponse().getParticipant() : null;
            if (p == null) continue;
            int earned = winningPickers.contains(p.getId()) ? pointsValue : 0;
            ParticipantScore ps = existingByPid.get(p.getId());
            if (ps == null) {
                ps = new ParticipantScore();
                ps.setParticipant(p);
                ps.setQuestion(question);
                ps.setRound(round);
            }
            ps.setPointsEarned(earned);
            participantScoreRepository.save(ps);
            existingByPid.remove(p.getId());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalResponses", answers.size());
        result.put("maxVotes", max);
        result.put("winningOptionIds", winningOptionIds);
        result.put("winningLabels", winnerLabels);
        result.put("winningPickerCount", winningPickers.size());
        result.put("pointsAwarded", pointsValue);
        return result;
    }

    public QuestionOptionDto updateOptionPoints(Integer optionId, Integer points) {
        QuestionOption option = questionOptionRepository.findById(optionId)
                .orElseThrow(() -> new NoSuchElementException("Option not found with id: " + optionId));

        option.setPoints(points);

        QuestionOption saved = questionOptionRepository.save(option);
        return QuestionOptionDto.fromEntity(saved);
    }
}
