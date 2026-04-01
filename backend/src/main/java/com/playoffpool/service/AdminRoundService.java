package com.playoffpool.service;

import com.playoffpool.dto.RoundDto;
import com.playoffpool.model.Question;
import com.playoffpool.model.Round;
import com.playoffpool.model.Season;
import com.playoffpool.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AdminRoundService {

    private final RoundRepository roundRepository;
    private final SeasonRepository seasonRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final ResponseAnswerRepository responseAnswerRepository;
    private final ResponseRepository responseRepository;
    private final ParticipantScoreRepository participantScoreRepository;

    public AdminRoundService(RoundRepository roundRepository, SeasonRepository seasonRepository,
                             QuestionRepository questionRepository, QuestionOptionRepository questionOptionRepository,
                             ResponseAnswerRepository responseAnswerRepository, ResponseRepository responseRepository,
                             ParticipantScoreRepository participantScoreRepository) {
        this.roundRepository = roundRepository;
        this.seasonRepository = seasonRepository;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.responseAnswerRepository = responseAnswerRepository;
        this.responseRepository = responseRepository;
        this.participantScoreRepository = participantScoreRepository;
    }

    public List<Round> getAllRounds() {
        return roundRepository.findAllByOrderByDisplayOrderAsc();
    }

    public List<Round> getRoundsBySeason(Integer seasonId) {
        return roundRepository.findBySeasonIdOrderByDisplayOrderAsc(seasonId);
    }

    public Round getRound(Integer id) {
        return roundRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Round not found with id: " + id));
    }

    public Round createRound(RoundDto dto) {
        Round round = new Round();
        dto.applyToEntity(round, roundRepository);
        if (dto.getSeasonId() != null) {
            Season season = seasonRepository.findById(dto.getSeasonId())
                    .orElseThrow(() -> new NoSuchElementException("Season not found with id: " + dto.getSeasonId()));
            round.setSeason(season);
        }
        round.setCreatedAt(LocalDateTime.now());
        return roundRepository.save(round);
    }

    public Round updateRound(Integer id, RoundDto dto) {
        Round round = getRound(id);
        dto.applyToEntity(round, roundRepository);
        return roundRepository.save(round);
    }

    @Transactional
    public void deleteRound(Integer id) {
        // Delete scores for this round
        participantScoreRepository.deleteAll(participantScoreRepository.findByRoundId(id));

        // Delete response answers and responses for this round
        List<com.playoffpool.model.Response> responses = responseRepository.findByRoundId(id);
        for (com.playoffpool.model.Response r : responses) {
            responseAnswerRepository.deleteAll(responseAnswerRepository.findByResponseId(r.getId()));
        }
        responseRepository.deleteAll(responses);

        // Delete options and questions for this round
        List<Question> questions = questionRepository.findByRoundIdOrderByDisplayOrder(id);
        for (Question q : questions) {
            questionOptionRepository.deleteAll(questionOptionRepository.findByQuestionIdOrderByDisplayOrder(q.getId()));
        }
        questionRepository.deleteAll(questions);

        // Also unlink any rounds that display_with this round
        List<Round> attached = roundRepository.findByDisplayWithRoundId(id);
        for (Round r : attached) {
            r.setDisplayWithRound(null);
            roundRepository.save(r);
        }

        roundRepository.deleteById(id);
    }

    public Round updateRoundStatus(Integer id, String newStatus) {
        Round round = getRound(id);
        String currentStatus = round.getStatus();

        List<String> validStatuses = List.of("draft", "open", "closed", "scored");
        if (!validStatuses.contains(newStatus)) {
            throw new IllegalArgumentException("Invalid status: '" + newStatus + "'");
        }

        round.setStatus(newStatus);
        return roundRepository.save(round);
    }
}
