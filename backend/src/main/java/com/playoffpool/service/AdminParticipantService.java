package com.playoffpool.service;

import com.playoffpool.dto.AnswerDto;
import com.playoffpool.dto.ParticipantResponseDto;
import com.playoffpool.model.*;
import com.playoffpool.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminParticipantService {

    private final ParticipantRepository participantRepository;
    private final ResponseRepository responseRepository;
    private final ResponseAnswerRepository responseAnswerRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final ParticipantScoreRepository participantScoreRepository;
    private final RoundRepository roundRepository;

    public AdminParticipantService(ParticipantRepository participantRepository,
                                   ResponseRepository responseRepository,
                                   ResponseAnswerRepository responseAnswerRepository,
                                   QuestionRepository questionRepository,
                                   QuestionOptionRepository questionOptionRepository,
                                   ParticipantScoreRepository participantScoreRepository,
                                   RoundRepository roundRepository) {
        this.participantRepository = participantRepository;
        this.responseRepository = responseRepository;
        this.responseAnswerRepository = responseAnswerRepository;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.participantScoreRepository = participantScoreRepository;
        this.roundRepository = roundRepository;
    }

    public List<Participant> getAllParticipants() {
        return participantRepository.findAll();
    }

    public List<Participant> getParticipantsBySeason(Integer seasonId) {
        return participantRepository.findBySeasonId(seasonId);
    }

    @Transactional
    public Participant updateParticipant(Integer id, String name, String email, String teamName, String division) {
        Participant participant = participantRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Participant not found"));
        participant.setName(name);
        participant.setEmail(email);
        participant.setTeamName(teamName);
        participant.setDivision(division);
        return participantRepository.save(participant);
    }

    @Transactional
    public Participant updatePaidStatus(Integer id, Boolean paid) {
        Participant participant = participantRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Participant not found"));
        participant.setPaid(paid);
        return participantRepository.save(participant);
    }

    @Transactional
    public void deleteParticipant(Integer id) {
        // Delete scores
        participantScoreRepository.deleteAll(participantScoreRepository.findByParticipantId(id));
        // Delete response answers and responses
        List<Response> responses = responseRepository.findByParticipantId(id);
        for (Response r : responses) {
            responseAnswerRepository.deleteAll(responseAnswerRepository.findByResponseId(r.getId()));
        }
        responseRepository.deleteAll(responses);
        participantRepository.deleteById(id);
    }

    @Transactional
    public void deleteResponse(Integer participantId, Integer roundId) {
        Response response = responseRepository.findByParticipantIdAndRoundId(participantId, roundId)
                .orElseThrow(() -> new NoSuchElementException("Response not found"));
        participantScoreRepository.deleteAll(
                participantScoreRepository.findByParticipantIdAndRoundId(participantId, roundId));
        responseAnswerRepository.deleteAll(responseAnswerRepository.findByResponseId(response.getId()));
        responseRepository.delete(response);
    }

    @Transactional(readOnly = true)
    public List<ParticipantResponseDto> getResponsesByRound(Integer roundId) {
        return buildResponseDtos(responseRepository.findByRoundId(roundId));
    }

    @Transactional(readOnly = true)
    public List<ParticipantResponseDto> getResponsesByParticipant(Integer participantId) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new NoSuchElementException("Participant not found"));

        List<Response> responses = responseRepository.findByParticipantId(participantId);
        Map<Integer, Response> responseByRoundId = responses.stream()
                .collect(Collectors.toMap(r -> r.getRound().getId(), r -> r));

        List<Round> allRounds = roundRepository.findBySeasonIdOrderByDisplayOrderAsc(
                participant.getSeason().getId());

        List<ParticipantResponseDto> result = buildResponseDtos(responses);
        Map<Integer, ParticipantResponseDto> dtoByRoundId = result.stream()
                .collect(Collectors.toMap(ParticipantResponseDto::getRoundId, d -> d));

        List<QuestionOption> allOptions = questionOptionRepository.findAll();
        Map<Integer, QuestionOption> optionMap = allOptions.stream()
                .collect(Collectors.toMap(QuestionOption::getId, o -> o));

        for (Round round : allRounds) {
            if (dtoByRoundId.containsKey(round.getId())) continue;

            List<Question> roundQuestions = questionRepository.findByRoundIdOrderByDisplayOrder(round.getId());
            List<AnswerDto> answerDtos = new ArrayList<>();
            for (Question question : roundQuestions) {
                AnswerDto answerDto = new AnswerDto();
                answerDto.setQuestionId(question.getId());
                answerDto.setQuestionTitle(question.getTitle());
                answerDto.setCorrectAnswerText(question.getCorrectAnswerText());
                answerDtos.add(answerDto);
            }

            ParticipantResponseDto dto = new ParticipantResponseDto();
            dto.setParticipantId(participant.getId());
            dto.setParticipantName(participant.getName());
            dto.setTeamName(participant.getTeamName());
            dto.setEmail(participant.getEmail());
            dto.setPaid(participant.getPaid());
            dto.setRoundId(round.getId());
            dto.setRoundName(round.getName());
            dto.setRoundDisplayOrder(round.getDisplayOrder());
            dto.setSubmittedAt(null);
            dto.setAnswers(answerDtos);
            dto.setRoundPointsTotal(null);
            result.add(dto);
        }

        result.sort(Comparator.comparingInt(r -> r.getRoundDisplayOrder() != null ? r.getRoundDisplayOrder() : 0));
        return result;
    }

    private List<ParticipantResponseDto> buildResponseDtos(List<Response> responses) {
        if (responses.isEmpty()) {
            return new ArrayList<>();
        }

        // Load scores for all participants in this result set
        Set<Integer> participantIds = responses.stream()
                .map(r -> r.getParticipant().getId())
                .collect(Collectors.toSet());
        Map<String, Integer> scoreMap = new java.util.HashMap<>(); // "participantId:questionId" -> points
        for (Integer pid : participantIds) {
            for (ParticipantScore ps : participantScoreRepository.findByParticipantId(pid)) {
                scoreMap.put(pid + ":" + ps.getQuestion().getId(), ps.getPointsEarned());
            }
        }

        List<Integer> responseIds = responses.stream()
                .map(Response::getId)
                .collect(Collectors.toList());

        List<ResponseAnswer> allAnswers = responseAnswerRepository.findByResponseIdIn(responseIds);

        // Build question map from all referenced questions
        Map<Integer, Question> questionMap = new java.util.HashMap<>();
        for (ResponseAnswer ra : allAnswers) {
            Integer qId = ra.getQuestion().getId();
            if (!questionMap.containsKey(qId)) {
                questionRepository.findById(qId).ifPresent(q -> questionMap.put(qId, q));
            }
        }

        List<QuestionOption> allOptions = questionOptionRepository.findAll();
        Map<Integer, QuestionOption> optionMap = allOptions.stream()
                .collect(Collectors.toMap(QuestionOption::getId, o -> o));

        Map<Integer, List<ResponseAnswer>> answersByResponseId = allAnswers.stream()
                .collect(Collectors.groupingBy(ra -> ra.getResponse().getId()));

        // Cache questions per round so we can show unanswered questions too
        Map<Integer, List<Question>> questionsByRound = new HashMap<>();

        List<ParticipantResponseDto> result = new ArrayList<>();

        for (Response response : responses) {
            ParticipantResponseDto dto = new ParticipantResponseDto();
            Participant participant = response.getParticipant();
            dto.setParticipantId(participant.getId());
            dto.setParticipantName(participant.getName());
            dto.setTeamName(participant.getTeamName());
            dto.setEmail(participant.getEmail());
            dto.setPaid(participant.getPaid());
            dto.setRoundId(response.getRound().getId());
            dto.setRoundName(response.getRound().getName());
            dto.setRoundDisplayOrder(response.getRound().getDisplayOrder());
            dto.setSubmittedAt(response.getSubmittedAt());

            List<ResponseAnswer> responseAnswers = answersByResponseId.getOrDefault(response.getId(), new ArrayList<>());

            // Group answers by question to merge multi-select into one AnswerDto
            Map<Integer, List<ResponseAnswer>> byQuestion = responseAnswers.stream()
                    .collect(Collectors.groupingBy(ra -> ra.getQuestion().getId()));

            // Load all questions for this round (cached)
            Integer rndId = response.getRound().getId();
            List<Question> roundQuestions = questionsByRound.computeIfAbsent(rndId,
                    id -> questionRepository.findByRoundIdOrderByDisplayOrder(id));

            List<AnswerDto> answerDtos = new ArrayList<>();
            for (Question question : roundQuestions) {
                AnswerDto answerDto = new AnswerDto();
                answerDto.setQuestionId(question.getId());
                answerDto.setQuestionTitle(question.getTitle());
                answerDto.setCorrectAnswerText(question.getCorrectAnswerText());

                List<ResponseAnswer> qAnswers = byQuestion.getOrDefault(question.getId(), Collections.emptyList());

                // Merge selected option texts for multi-select and collect point values
                List<String> optionTexts = new ArrayList<>();
                Integer totalPointValue = null;
                for (ResponseAnswer ra : qAnswers) {
                    QuestionOption selectedOption = ra.getSelectedOption();
                    if (selectedOption != null) {
                        QuestionOption option = optionMap.get(selectedOption.getId());
                        if (option != null) {
                            optionTexts.add(option.getOptionText());
                            if (option.getPoints() != null) {
                                totalPointValue = (totalPointValue != null ? totalPointValue : 0) + option.getPoints();
                            }
                        }
                    }
                    if (ra.getFreeFormValue() != null) {
                        answerDto.setFreeFormValue(ra.getFreeFormValue());
                    }
                }
                if (!optionTexts.isEmpty()) {
                    answerDto.setSelectedOptionText(String.join(", ", optionTexts));
                }
                answerDto.setOptionPointValue(totalPointValue);

                // Set points from score map
                Integer pts = scoreMap.get(participant.getId() + ":" + question.getId());
                answerDto.setPointsEarned(pts);

                answerDtos.add(answerDto);
            }

            dto.setAnswers(answerDtos);

            // Compute round total
            int total = answerDtos.stream()
                    .filter(a -> a.getPointsEarned() != null)
                    .mapToInt(AnswerDto::getPointsEarned)
                    .sum();
            boolean hasAnyScores = answerDtos.stream().anyMatch(a -> a.getPointsEarned() != null);
            dto.setRoundPointsTotal(hasAnyScores ? total : null);

            result.add(dto);
        }

        result.sort(Comparator.comparingInt(r -> r.getRoundDisplayOrder() != null ? r.getRoundDisplayOrder() : 0));
        return result;
    }
}
