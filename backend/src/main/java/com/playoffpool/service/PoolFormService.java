package com.playoffpool.service;

import com.playoffpool.dto.*;
import com.playoffpool.model.*;
import com.playoffpool.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PoolFormService {

    private final ParticipantRepository participantRepository;
    private final RoundRepository roundRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final ResponseRepository responseRepository;
    private final ResponseAnswerRepository responseAnswerRepository;
    private final SeasonRepository seasonRepository;

    public PoolFormService(ParticipantRepository participantRepository,
                           RoundRepository roundRepository,
                           QuestionRepository questionRepository,
                           QuestionOptionRepository questionOptionRepository,
                           ResponseRepository responseRepository,
                           ResponseAnswerRepository responseAnswerRepository,
                           SeasonRepository seasonRepository) {
        this.participantRepository = participantRepository;
        this.roundRepository = roundRepository;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.responseRepository = responseRepository;
        this.responseAnswerRepository = responseAnswerRepository;
        this.seasonRepository = seasonRepository;
    }

    public Optional<Participant> findParticipantByEmail(String email) {
        return participantRepository.findByEmail(email);
    }

    public Optional<Participant> findParticipantByEmailAndSeason(String email, Integer seasonId) {
        return participantRepository.findByEmailAndSeasonId(email, seasonId);
    }

    @Transactional
    public Participant registerParticipant(String email, String name, String teamName, Integer seasonId) {
        if (seasonId != null) {
            Optional<Participant> existing = participantRepository.findByEmailAndSeasonId(email, seasonId);
            if (existing.isPresent()) {
                throw new IllegalArgumentException("Email is already registered for this season");
            }
        }
        Season season = null;
        if (seasonId != null) {
            season = seasonRepository.findById(seasonId)
                    .orElseThrow(() -> new NoSuchElementException("Season not found with id: " + seasonId));
        }
        Participant p = new Participant();
        p.setEmail(email);
        p.setName(name);
        p.setTeamName(teamName);
        p.setSeason(season);
        p.setCreatedAt(LocalDateTime.now());
        return participantRepository.save(p);
    }

    @Transactional
    public Participant updateParticipantProfile(Integer participantId, String name, String teamName) {
        Participant p = participantRepository.findById(participantId)
                .orElseThrow(() -> new NoSuchElementException("Participant not found"));
        p.setName(name);
        p.setTeamName(teamName);
        return participantRepository.save(p);
    }

    @Transactional(readOnly = true)
    public PoolFormDto getRoundForm(Integer participantId, Integer roundId) {
        return getRoundForm(participantId, roundId, null);
    }

    @Transactional(readOnly = true)
    public PoolFormDto getRoundForm(Integer participantId, Integer roundId, Integer seasonId) {
        Round openRound;
        if (roundId != null) {
            openRound = roundRepository.findById(roundId)
                    .orElseThrow(() -> new NoSuchElementException("Round not found"));
            if (!"open".equals(openRound.getStatus())) {
                throw new IllegalArgumentException("This round is not currently open for picks");
            }
        } else {
            List<Round> openRounds;
            if (seasonId != null) {
                openRounds = roundRepository.findBySeasonIdAndStatusAndDisplayWithRoundIdIsNull(seasonId, "open");
            } else {
                openRounds = roundRepository.findByStatusAndDisplayWithRoundIdIsNull("open");
            }
            if (openRounds.isEmpty()) {
                throw new NoSuchElementException("No round is currently open");
            }
            openRound = openRounds.get(0);
        }

        // Find attached rounds
        List<Round> attachedRounds = roundRepository.findByDisplayWithRoundId(openRound.getId());

        // Sort all rounds by displayOrder
        List<Round> allRoundsSorted = new ArrayList<>();
        allRoundsSorted.add(openRound);
        allRoundsSorted.addAll(attachedRounds);
        allRoundsSorted.sort(Comparator.comparingInt(r -> r.getDisplayOrder() != null ? r.getDisplayOrder() : Integer.MAX_VALUE));

        // Build question list in round displayOrder
        List<Question> allQuestions = new ArrayList<>();
        for (Round r : allRoundsSorted) {
            allQuestions.addAll(questionRepository.findByRoundIdOrderByDisplayOrder(r.getId()));
        }

        // Batch-load all options
        List<Integer> questionIds = allQuestions.stream()
                .map(Question::getId)
                .collect(Collectors.toList());

        Map<Integer, List<QuestionOption>> optionsByQuestionId = new HashMap<>();
        if (!questionIds.isEmpty()) {
            List<QuestionOption> allOptions = questionOptionRepository.findByQuestionIdInOrderByDisplayOrder(questionIds);
            for (QuestionOption opt : allOptions) {
                optionsByQuestionId
                        .computeIfAbsent(opt.getQuestion().getId(), k -> new ArrayList<>())
                        .add(opt);
            }
        }

        // Build FormQuestionDto list
        List<FormQuestionDto> questionDtos = allQuestions.stream()
                .map(q -> FormQuestionDto.fromEntity(q, optionsByQuestionId.getOrDefault(q.getId(), Collections.emptyList())))
                .collect(Collectors.toList());

        // Check if participant already submitted
        boolean alreadySubmitted = false;
        if (participantId != null) {
            // Check response exists for the open round
            Optional<Response> openResponse = responseRepository.findByParticipantIdAndRoundId(participantId, openRound.getId());
            if (openResponse.isPresent()) {
                alreadySubmitted = true;
            }
            // Also check attached rounds
            if (!alreadySubmitted) {
                for (Round attached : attachedRounds) {
                    Optional<Response> attachedResponse = responseRepository.findByParticipantIdAndRoundId(participantId, attached.getId());
                    if (attachedResponse.isPresent()) {
                        alreadySubmitted = true;
                        break;
                    }
                }
            }
        }

        // Build PoolFormDto
        PoolFormDto formDto = new PoolFormDto();
        formDto.setRound(RoundDto.fromEntity(openRound));
        formDto.setQuestions(questionDtos);
        formDto.setDeadline(openRound.getDeadline() != null ? openRound.getDeadline().toString() : null);
        formDto.setAlreadySubmitted(alreadySubmitted);
        return formDto;
    }

    @Transactional
    public void submitPicks(SubmitPicksDto dto) {
        // Find participant first to determine season
        Participant participant = participantRepository.findById(dto.getParticipantId())
                .orElseThrow(() -> new NoSuchElementException("Participant not found"));

        // Find open round scoped to participant's season
        Integer seasonId = participant.getSeason().getId();
        List<Round> openRounds = roundRepository.findBySeasonIdAndStatusAndDisplayWithRoundIdIsNull(seasonId, "open");
        if (openRounds.isEmpty()) {
            throw new NoSuchElementException("No round is currently open");
        }
        Round openRound = openRounds.get(0);

        // Validate deadline
        if (openRound.getDeadline() != null && !LocalDateTime.now().isBefore(openRound.getDeadline())) {
            throw new IllegalArgumentException("The deadline for this round has passed");
        }

        // Check no existing response for open round
        Optional<Response> existingResponse = responseRepository.findByParticipantIdAndRoundId(participant.getId(), openRound.getId());
        if (existingResponse.isPresent()) {
            throw new IllegalArgumentException("You have already submitted picks for this round");
        }

        // Load all questions for the round + attached rounds
        List<Question> primaryQuestions = questionRepository.findByRoundIdOrderByDisplayOrder(openRound.getId());
        List<Round> attachedRounds = roundRepository.findByDisplayWithRoundId(openRound.getId());

        List<Question> allQuestions = new ArrayList<>(primaryQuestions);
        for (Round attached : attachedRounds) {
            allQuestions.addAll(questionRepository.findByRoundIdOrderByDisplayOrder(attached.getId()));
        }

        // Build map of questionId -> Question
        Map<Integer, Question> questionMap = new HashMap<>();
        for (Question q : allQuestions) {
            questionMap.put(q.getId(), q);
        }

        // Build map of questionId -> PickAnswerDto
        Map<Integer, PickAnswerDto> answerMap = new HashMap<>();
        if (dto.getAnswers() != null) {
            for (PickAnswerDto answer : dto.getAnswers()) {
                answerMap.put(answer.getQuestionId(), answer);
            }
        }

        // Validate mandatory questions have answers
        for (Question q : allQuestions) {
            if (Boolean.TRUE.equals(q.getIsMandatory())) {
                PickAnswerDto answer = answerMap.get(q.getId());
                if (answer == null) {
                    throw new IllegalArgumentException("Mandatory question '" + q.getTitle() + "' must be answered");
                }
                String type = q.getQuestionType();
                if ("jeopardy".equals(type)) {
                    if (answer.getSelectedOptionId() == null) {
                        throw new IllegalArgumentException("Mandatory question '" + q.getTitle() + "' must be answered");
                    }
                } else if ("multi_select".equals(type) || "box".equals(type)) {
                    if (answer.getSelectedOptionIds() == null || answer.getSelectedOptionIds().isEmpty()) {
                        throw new IllegalArgumentException("Mandatory question '" + q.getTitle() + "' must be answered");
                    }
                } else if ("free_form".equals(type) || "number_of_games".equals(type)) {
                    if (answer.getFreeFormValue() == null || answer.getFreeFormValue().trim().isEmpty()) {
                        throw new IllegalArgumentException("Mandatory question '" + q.getTitle() + "' must be answered");
                    }
                }
            }
        }

        // Validate max_selections
        if (dto.getAnswers() != null) {
            for (PickAnswerDto answer : dto.getAnswers()) {
                Question q = questionMap.get(answer.getQuestionId());
                if (q != null && "multi_select".equals(q.getQuestionType()) && q.getMaxSelections() != null) {
                    if (answer.getSelectedOptionIds() != null && answer.getSelectedOptionIds().size() > q.getMaxSelections()) {
                        throw new IllegalArgumentException("Question '" + q.getTitle() + "' allows at most " + q.getMaxSelections() + " selections");
                    }
                }
            }
        }

        // Validate jeopardy wagers
        if (dto.getAnswers() != null) {
            for (PickAnswerDto answer : dto.getAnswers()) {
                Question q = questionMap.get(answer.getQuestionId());
                if (q != null && "jeopardy".equals(q.getQuestionType()) && answer.getFreeFormValue() != null) {
                    try {
                        int wager = Integer.parseInt(answer.getFreeFormValue());
                        if (wager <= 0) {
                            throw new IllegalArgumentException("Wager for '" + q.getTitle() + "' must be positive");
                        }
                        if (q.getMaxWager() != null && wager > q.getMaxWager()) {
                            throw new IllegalArgumentException("Wager for '" + q.getTitle() + "' exceeds maximum of " + q.getMaxWager());
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Wager for '" + q.getTitle() + "' must be a number");
                    }
                }
            }
        }

        // Group answers by roundId
        Map<Integer, List<PickAnswerDto>> answersByRoundId = new HashMap<>();
        if (dto.getAnswers() != null) {
            for (PickAnswerDto answer : dto.getAnswers()) {
                Question q = questionMap.get(answer.getQuestionId());
                if (q != null) {
                    Integer roundId = q.getRound().getId();
                    answersByRoundId.computeIfAbsent(roundId, k -> new ArrayList<>()).add(answer);
                }
            }
        }

        // Collect all round IDs we need responses for
        Set<Integer> allRoundIds = new HashSet<>();
        allRoundIds.add(openRound.getId());
        for (Round attached : attachedRounds) {
            allRoundIds.add(attached.getId());
        }

        // Build a map of roundId -> Round for lookup
        Map<Integer, Round> roundMap = new HashMap<>();
        roundMap.put(openRound.getId(), openRound);
        for (Round attached : attachedRounds) {
            roundMap.put(attached.getId(), attached);
        }

        // For each round with answers, create a Response and ResponseAnswers
        for (Integer roundId : allRoundIds) {
            List<PickAnswerDto> roundAnswers = answersByRoundId.getOrDefault(roundId, Collections.emptyList());
            if (roundAnswers.isEmpty() && !roundId.equals(openRound.getId())) {
                continue;
            }

            Response response = new Response();
            response.setParticipant(participant);
            response.setRound(roundMap.get(roundId));
            response.setSubmittedAt(LocalDateTime.now());
            response = responseRepository.save(response);

            List<ResponseAnswer> responseAnswers = new ArrayList<>();
            for (PickAnswerDto answer : roundAnswers) {
                Question q = questionMap.get(answer.getQuestionId());
                if (q == null) {
                    continue;
                }
                String type = q.getQuestionType();

                if ("jeopardy".equals(type)) {
                    ResponseAnswer ra = new ResponseAnswer();
                    ra.setResponse(response);
                    ra.setQuestion(q);
                    if (answer.getSelectedOptionId() != null) {
                        QuestionOption option = questionOptionRepository.findById(answer.getSelectedOptionId()).orElse(null);
                        ra.setSelectedOption(option);
                    }
                    ra.setFreeFormValue(answer.getFreeFormValue());
                    responseAnswers.add(ra);
                } else if ("multi_select".equals(type) || "box".equals(type)) {
                    if (answer.getSelectedOptionIds() != null) {
                        for (Integer optionId : answer.getSelectedOptionIds()) {
                            ResponseAnswer ra = new ResponseAnswer();
                            ra.setResponse(response);
                            ra.setQuestion(q);
                            QuestionOption option = questionOptionRepository.findById(optionId).orElse(null);
                            ra.setSelectedOption(option);
                            responseAnswers.add(ra);
                        }
                    }
                } else if ("free_form".equals(type) || "number_of_games".equals(type)) {
                    ResponseAnswer ra = new ResponseAnswer();
                    ra.setResponse(response);
                    ra.setQuestion(q);
                    ra.setFreeFormValue(answer.getFreeFormValue());
                    responseAnswers.add(ra);
                }
            }

            if (!responseAnswers.isEmpty()) {
                responseAnswerRepository.saveAll(responseAnswers);
            }
        }
    }
}
