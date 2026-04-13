package com.playoffpool.service;

import com.playoffpool.dto.RoundDto;
import com.playoffpool.model.Question;
import com.playoffpool.model.Response;
import com.playoffpool.model.Round;
import com.playoffpool.model.Season;
import com.playoffpool.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminRoundServiceTest {

    @Mock private RoundRepository roundRepository;
    @Mock private SeasonRepository seasonRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private QuestionOptionRepository questionOptionRepository;
    @Mock private ResponseAnswerRepository responseAnswerRepository;
    @Mock private ResponseRepository responseRepository;
    @Mock private ParticipantScoreRepository participantScoreRepository;

    @InjectMocks
    private AdminRoundService service;

    private Season season;
    private Round round;

    @BeforeEach
    void setUp() {
        season = new Season();
        season.setId(1);
        season.setName("2025 Playoffs");
        season.setYear(2025);

        round = new Round();
        round.setId(1);
        round.setName("Round 1");
        round.setStatus("draft");
        round.setDisplayOrder(1);
        round.setSeason(season);
        round.setDeadline(LocalDateTime.of(2025, 4, 15, 18, 0));
        round.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getAllRounds_returnsOrderedList() {
        Round r2 = new Round();
        r2.setId(2);
        r2.setName("Round 2");
        r2.setDisplayOrder(2);
        r2.setSeason(season);
        when(roundRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(List.of(round, r2));

        List<Round> result = service.getAllRounds();

        assertEquals(2, result.size());
        assertEquals("Round 1", result.get(0).getName());
    }

    @Test
    void getRoundsBySeason_filtersCorrectly() {
        when(roundRepository.findBySeasonIdOrderByDisplayOrderAsc(1)).thenReturn(List.of(round));

        List<Round> result = service.getRoundsBySeason(1);

        assertEquals(1, result.size());
    }

    @Test
    void getRound_existingId_returnsRound() {
        when(roundRepository.findById(1)).thenReturn(Optional.of(round));

        Round result = service.getRound(1);

        assertEquals("Round 1", result.getName());
    }

    @Test
    void getRound_nonExistingId_throwsException() {
        when(roundRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getRound(999));
    }

    @Test
    void createRound_withSeason_setsSeason() {
        when(seasonRepository.findById(1)).thenReturn(Optional.of(season));
        when(roundRepository.save(any(Round.class))).thenAnswer(inv -> inv.getArgument(0));

        RoundDto dto = new RoundDto();
        dto.setName("New Round");
        dto.setSeasonId(1);
        dto.setStatus("draft");
        dto.setDisplayOrder(1);
        dto.setDeadline(LocalDateTime.of(2025, 5, 1, 18, 0));

        Round result = service.createRound(dto);

        assertEquals(season, result.getSeason());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void createRound_nonExistingSeason_throwsException() {
        when(seasonRepository.findById(999)).thenReturn(Optional.empty());

        RoundDto dto = new RoundDto();
        dto.setName("New Round");
        dto.setSeasonId(999);
        dto.setStatus("draft");
        dto.setDisplayOrder(1);

        assertThrows(NoSuchElementException.class, () -> service.createRound(dto));
    }

    @Test
    void updateRound_updatesFields() {
        when(roundRepository.findById(1)).thenReturn(Optional.of(round));
        when(roundRepository.save(any(Round.class))).thenAnswer(inv -> inv.getArgument(0));

        RoundDto dto = new RoundDto();
        dto.setName("Updated Round");
        dto.setStatus("open");
        dto.setDisplayOrder(2);
        dto.setDeadline(LocalDateTime.of(2025, 6, 1, 18, 0));

        Round result = service.updateRound(1, dto);

        assertEquals("Updated Round", result.getName());
    }

    @Test
    void deleteRound_cascadesAllRelatedData() {
        when(participantScoreRepository.findByRoundId(1)).thenReturn(Collections.emptyList());

        Response response = new Response();
        response.setId(10);
        when(responseRepository.findByRoundId(1)).thenReturn(List.of(response));
        when(responseAnswerRepository.findByResponseId(10)).thenReturn(Collections.emptyList());

        Question question = new Question();
        question.setId(20);
        when(questionRepository.findByRoundIdOrderByDisplayOrder(1)).thenReturn(List.of(question));
        when(questionOptionRepository.findByQuestionIdOrderByDisplayOrder(20)).thenReturn(Collections.emptyList());

        when(roundRepository.findByDisplayWithRoundId(1)).thenReturn(Collections.emptyList());

        service.deleteRound(1);

        verify(participantScoreRepository).deleteAll(any());
        verify(responseAnswerRepository).deleteAll(any());
        verify(responseRepository).deleteAll(any());
        verify(questionOptionRepository).deleteAll(any());
        verify(questionRepository).deleteAll(any());
        verify(roundRepository).deleteById(1);
    }

    @Test
    void deleteRound_unlinksAttachedRounds() {
        when(participantScoreRepository.findByRoundId(1)).thenReturn(Collections.emptyList());
        when(responseRepository.findByRoundId(1)).thenReturn(Collections.emptyList());
        when(questionRepository.findByRoundIdOrderByDisplayOrder(1)).thenReturn(Collections.emptyList());

        Round attached = new Round();
        attached.setId(2);
        attached.setDisplayWithRound(round);
        when(roundRepository.findByDisplayWithRoundId(1)).thenReturn(List.of(attached));

        service.deleteRound(1);

        assertNull(attached.getDisplayWithRound());
        verify(roundRepository).save(attached);
    }

    @Test
    void updateRoundStatus_validStatus_updatesSuccessfully() {
        when(roundRepository.findById(1)).thenReturn(Optional.of(round));
        when(roundRepository.save(any(Round.class))).thenAnswer(inv -> inv.getArgument(0));

        Round result = service.updateRoundStatus(1, "open");

        assertEquals("open", result.getStatus());
    }

    @Test
    void updateRoundStatus_invalidStatus_throwsException() {
        when(roundRepository.findById(1)).thenReturn(Optional.of(round));

        assertThrows(IllegalArgumentException.class,
                () -> service.updateRoundStatus(1, "invalid_status"));
    }

    @Test
    void updateRoundStatus_allValidStatuses() {
        for (String status : List.of("draft", "open", "closed", "scored")) {
            when(roundRepository.findById(1)).thenReturn(Optional.of(round));
            when(roundRepository.save(any(Round.class))).thenAnswer(inv -> inv.getArgument(0));

            Round result = service.updateRoundStatus(1, status);

            assertEquals(status, result.getStatus());
        }
    }
}
