package com.playoffpool.service;

import com.playoffpool.dto.PickAnswerDto;
import com.playoffpool.dto.PoolFormDto;
import com.playoffpool.dto.SubmitPicksDto;
import com.playoffpool.model.*;
import com.playoffpool.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PoolFormServiceTest {

    @Mock private ParticipantRepository participantRepository;
    @Mock private RoundRepository roundRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private QuestionOptionRepository questionOptionRepository;
    @Mock private ResponseRepository responseRepository;
    @Mock private ResponseAnswerRepository responseAnswerRepository;
    @Mock private SeasonRepository seasonRepository;

    @InjectMocks
    private PoolFormService service;

    private Season season;
    private Round openRound;
    private Participant participant;
    private Question mandatoryQuestion;
    private QuestionOption option1;

    @BeforeEach
    void setUp() {
        season = new Season();
        season.setId(1);
        season.setName("2025 Playoffs");

        openRound = new Round();
        openRound.setId(1);
        openRound.setName("Round 1");
        openRound.setStatus("open");
        openRound.setSeason(season);
        openRound.setDeadline(LocalDateTime.now().plusDays(7));
        openRound.setDisplayOrder(1);

        participant = new Participant();
        participant.setId(1);
        participant.setName("Test Player");
        participant.setEmail("test@example.com");
        participant.setSeason(season);

        mandatoryQuestion = new Question();
        mandatoryQuestion.setId(1);
        mandatoryQuestion.setRound(openRound);
        mandatoryQuestion.setTitle("Pick a team");
        mandatoryQuestion.setQuestionType("multi_select");
        mandatoryQuestion.setIsMandatory(true);
        mandatoryQuestion.setDisplayOrder(1);
        mandatoryQuestion.setMaxSelections(4);

        option1 = new QuestionOption();
        option1.setId(1);
        option1.setQuestion(mandatoryQuestion);
        option1.setOptionText("Team A");
        option1.setDisplayOrder(1);
        option1.setPoints(10);
    }

    // --- Lookup tests ---

    @Test
    void findParticipantByEmail_found() {
        when(participantRepository.findByEmail("test@example.com")).thenReturn(Optional.of(participant));

        Optional<Participant> result = service.findParticipantByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("Test Player", result.get().getName());
    }

    @Test
    void findParticipantByEmail_notFound() {
        when(participantRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Optional<Participant> result = service.findParticipantByEmail("unknown@example.com");

        assertFalse(result.isPresent());
    }

    @Test
    void findParticipantByEmailAndSeason_found() {
        when(participantRepository.findByEmailAndSeasonId("test@example.com", 1))
                .thenReturn(Optional.of(participant));

        Optional<Participant> result = service.findParticipantByEmailAndSeason("test@example.com", 1);

        assertTrue(result.isPresent());
    }

    // --- Registration tests ---

    @Test
    void registerParticipant_newParticipant_createsSuccessfully() {
        when(participantRepository.findByEmailAndSeasonId("new@example.com", 1)).thenReturn(Optional.empty());
        when(seasonRepository.findById(1)).thenReturn(Optional.of(season));
        when(participantRepository.save(any(Participant.class))).thenAnswer(inv -> {
            Participant p = inv.getArgument(0);
            p.setId(2);
            return p;
        });

        Participant result = service.registerParticipant("new@example.com", "New Player", "Team New", 1);

        assertEquals("new@example.com", result.getEmail());
        assertEquals("New Player", result.getName());
        assertEquals("Team New", result.getTeamName());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void registerParticipant_duplicateEmail_throwsException() {
        when(participantRepository.findByEmailAndSeasonId("test@example.com", 1))
                .thenReturn(Optional.of(participant));

        assertThrows(IllegalArgumentException.class,
                () -> service.registerParticipant("test@example.com", "Name", "Team", 1));
    }

    @Test
    void registerParticipant_nonExistingSeason_throwsException() {
        when(participantRepository.findByEmailAndSeasonId("test@example.com", 999)).thenReturn(Optional.empty());
        when(seasonRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.registerParticipant("test@example.com", "Name", "Team", 999));
    }

    // --- Form retrieval tests ---

    @Test
    void getRoundForm_withSpecificRound_returnsForm() {
        when(roundRepository.findById(1)).thenReturn(Optional.of(openRound));
        when(questionRepository.findByRoundIdOrderByDisplayOrder(1)).thenReturn(List.of(mandatoryQuestion));
        when(roundRepository.findByDisplayWithRoundId(1)).thenReturn(Collections.emptyList());
        when(questionOptionRepository.findByQuestionIdInOrderByDisplayOrder(List.of(1)))
                .thenReturn(List.of(option1));

        PoolFormDto result = service.getRoundForm(null, 1, null);

        assertEquals("Round 1", result.getRound().getName());
        assertEquals(1, result.getQuestions().size());
        assertFalse(result.isAlreadySubmitted());
    }

    @Test
    void getRoundForm_closedRound_throwsException() {
        Round closedRound = new Round();
        closedRound.setId(2);
        closedRound.setStatus("closed");
        closedRound.setSeason(season);
        when(roundRepository.findById(2)).thenReturn(Optional.of(closedRound));

        assertThrows(IllegalArgumentException.class, () -> service.getRoundForm(null, 2, null));
    }

    @Test
    void getRoundForm_noOpenRound_throwsException() {
        when(roundRepository.findByStatus("open")).thenReturn(Collections.emptyList());

        assertThrows(NoSuchElementException.class, () -> service.getRoundForm(null, null, null));
    }

    @Test
    void getRoundForm_participantAlreadySubmitted_flagsAsSubmitted() {
        when(roundRepository.findById(1)).thenReturn(Optional.of(openRound));
        when(questionRepository.findByRoundIdOrderByDisplayOrder(1)).thenReturn(Collections.emptyList());
        when(roundRepository.findByDisplayWithRoundId(1)).thenReturn(Collections.emptyList());
        when(responseRepository.findByParticipantIdAndRoundId(1, 1))
                .thenReturn(Optional.of(new Response()));

        PoolFormDto result = service.getRoundForm(1, 1, null);

        assertTrue(result.isAlreadySubmitted());
    }

    @Test
    void getRoundForm_withSeasonFilter_usesSeasonFilteredRounds() {
        when(roundRepository.findBySeasonIdAndStatus(1, "open")).thenReturn(List.of(openRound));
        when(questionRepository.findByRoundIdOrderByDisplayOrder(1)).thenReturn(Collections.emptyList());
        when(roundRepository.findByDisplayWithRoundId(1)).thenReturn(Collections.emptyList());

        PoolFormDto result = service.getRoundForm(null, null, 1);

        assertEquals("Round 1", result.getRound().getName());
    }

    // --- Submit picks tests ---

    @Test
    void submitPicks_validSubmission_savesSuccessfully() {
        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(roundRepository.findBySeasonIdAndStatus(1, "open")).thenReturn(List.of(openRound));
        when(responseRepository.findByParticipantIdAndRoundId(1, 1)).thenReturn(Optional.empty());
        when(questionRepository.findByRoundIdOrderByDisplayOrder(1)).thenReturn(List.of(mandatoryQuestion));
        when(roundRepository.findByDisplayWithRoundId(1)).thenReturn(Collections.emptyList());
        when(questionOptionRepository.findById(1)).thenReturn(Optional.of(option1));
        when(responseRepository.save(any(Response.class))).thenAnswer(inv -> {
            Response r = inv.getArgument(0);
            r.setId(1);
            return r;
        });
        when(responseAnswerRepository.saveAll(any())).thenReturn(Collections.emptyList());

        PickAnswerDto answer = new PickAnswerDto();
        answer.setQuestionId(1);
        answer.setSelectedOptionIds(List.of(1));

        SubmitPicksDto dto = new SubmitPicksDto();
        dto.setParticipantId(1);
        dto.setAnswers(List.of(answer));

        assertDoesNotThrow(() -> service.submitPicks(dto));
        verify(responseRepository).save(any(Response.class));
    }

    @Test
    void submitPicks_noOpenRound_throwsException() {
        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(roundRepository.findBySeasonIdAndStatus(1, "open")).thenReturn(Collections.emptyList());

        SubmitPicksDto dto = new SubmitPicksDto();
        dto.setParticipantId(1);
        dto.setAnswers(Collections.emptyList());

        assertThrows(NoSuchElementException.class, () -> service.submitPicks(dto));
    }

    @Test
    void submitPicks_pastDeadline_throwsException() {
        openRound.setDeadline(LocalDateTime.now().minusDays(1));

        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(roundRepository.findBySeasonIdAndStatus(1, "open")).thenReturn(List.of(openRound));

        SubmitPicksDto dto = new SubmitPicksDto();
        dto.setParticipantId(1);
        dto.setAnswers(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> service.submitPicks(dto));
    }

    @Test
    void submitPicks_alreadySubmitted_throwsException() {
        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(roundRepository.findBySeasonIdAndStatus(1, "open")).thenReturn(List.of(openRound));
        when(responseRepository.findByParticipantIdAndRoundId(1, 1))
                .thenReturn(Optional.of(new Response()));

        SubmitPicksDto dto = new SubmitPicksDto();
        dto.setParticipantId(1);
        dto.setAnswers(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> service.submitPicks(dto));
    }

    @Test
    void submitPicks_mandatoryQuestionUnanswered_throwsException() {
        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(roundRepository.findBySeasonIdAndStatus(1, "open")).thenReturn(List.of(openRound));
        when(responseRepository.findByParticipantIdAndRoundId(1, 1)).thenReturn(Optional.empty());
        when(questionRepository.findByRoundIdOrderByDisplayOrder(1)).thenReturn(List.of(mandatoryQuestion));
        when(roundRepository.findByDisplayWithRoundId(1)).thenReturn(Collections.emptyList());

        SubmitPicksDto dto = new SubmitPicksDto();
        dto.setParticipantId(1);
        dto.setAnswers(Collections.emptyList()); // No answers for mandatory question

        assertThrows(IllegalArgumentException.class, () -> service.submitPicks(dto));
    }

    @Test
    void submitPicks_mandatoryMultiSelectEmpty_throwsException() {
        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(roundRepository.findBySeasonIdAndStatus(1, "open")).thenReturn(List.of(openRound));
        when(responseRepository.findByParticipantIdAndRoundId(1, 1)).thenReturn(Optional.empty());
        when(questionRepository.findByRoundIdOrderByDisplayOrder(1)).thenReturn(List.of(mandatoryQuestion));
        when(roundRepository.findByDisplayWithRoundId(1)).thenReturn(Collections.emptyList());

        PickAnswerDto answer = new PickAnswerDto();
        answer.setQuestionId(1);
        answer.setSelectedOptionIds(Collections.emptyList()); // Empty multi_select

        SubmitPicksDto dto = new SubmitPicksDto();
        dto.setParticipantId(1);
        dto.setAnswers(List.of(answer));

        assertThrows(IllegalArgumentException.class, () -> service.submitPicks(dto));
    }

    @Test
    void submitPicks_exceedsMaxSelections_throwsException() {
        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(roundRepository.findBySeasonIdAndStatus(1, "open")).thenReturn(List.of(openRound));
        when(responseRepository.findByParticipantIdAndRoundId(1, 1)).thenReturn(Optional.empty());
        when(questionRepository.findByRoundIdOrderByDisplayOrder(1)).thenReturn(List.of(mandatoryQuestion));
        when(roundRepository.findByDisplayWithRoundId(1)).thenReturn(Collections.emptyList());

        PickAnswerDto answer = new PickAnswerDto();
        answer.setQuestionId(1);
        answer.setSelectedOptionIds(List.of(1, 2, 3, 4, 5)); // Exceeds maxSelections=4

        SubmitPicksDto dto = new SubmitPicksDto();
        dto.setParticipantId(1);
        dto.setAnswers(List.of(answer));

        assertThrows(IllegalArgumentException.class, () -> service.submitPicks(dto));
    }

    @Test
    void submitPicks_jeopardyWagerExceedsMax_throwsException() {
        Question jeopardyQ = new Question();
        jeopardyQ.setId(2);
        jeopardyQ.setRound(openRound);
        jeopardyQ.setTitle("Jeopardy Q");
        jeopardyQ.setQuestionType("jeopardy");
        jeopardyQ.setIsMandatory(true);
        jeopardyQ.setDisplayOrder(1);
        jeopardyQ.setMaxWager(50);

        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(roundRepository.findBySeasonIdAndStatus(1, "open")).thenReturn(List.of(openRound));
        when(responseRepository.findByParticipantIdAndRoundId(1, 1)).thenReturn(Optional.empty());
        when(questionRepository.findByRoundIdOrderByDisplayOrder(1)).thenReturn(List.of(jeopardyQ));
        when(roundRepository.findByDisplayWithRoundId(1)).thenReturn(Collections.emptyList());

        PickAnswerDto answer = new PickAnswerDto();
        answer.setQuestionId(2);
        answer.setSelectedOptionId(1);
        answer.setFreeFormValue("100"); // Exceeds maxWager=50

        SubmitPicksDto dto = new SubmitPicksDto();
        dto.setParticipantId(1);
        dto.setAnswers(List.of(answer));

        assertThrows(IllegalArgumentException.class, () -> service.submitPicks(dto));
    }

    @Test
    void submitPicks_jeopardyNonNumericWager_throwsException() {
        Question jeopardyQ = new Question();
        jeopardyQ.setId(2);
        jeopardyQ.setRound(openRound);
        jeopardyQ.setTitle("Jeopardy Q");
        jeopardyQ.setQuestionType("jeopardy");
        jeopardyQ.setIsMandatory(true);
        jeopardyQ.setDisplayOrder(1);
        jeopardyQ.setMaxWager(50);

        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(roundRepository.findBySeasonIdAndStatus(1, "open")).thenReturn(List.of(openRound));
        when(responseRepository.findByParticipantIdAndRoundId(1, 1)).thenReturn(Optional.empty());
        when(questionRepository.findByRoundIdOrderByDisplayOrder(1)).thenReturn(List.of(jeopardyQ));
        when(roundRepository.findByDisplayWithRoundId(1)).thenReturn(Collections.emptyList());

        PickAnswerDto answer = new PickAnswerDto();
        answer.setQuestionId(2);
        answer.setSelectedOptionId(1);
        answer.setFreeFormValue("not_a_number");

        SubmitPicksDto dto = new SubmitPicksDto();
        dto.setParticipantId(1);
        dto.setAnswers(List.of(answer));

        assertThrows(IllegalArgumentException.class, () -> service.submitPicks(dto));
    }

    @Test
    void submitPicks_jeopardyNegativeWager_throwsException() {
        Question jeopardyQ = new Question();
        jeopardyQ.setId(2);
        jeopardyQ.setRound(openRound);
        jeopardyQ.setTitle("Jeopardy Q");
        jeopardyQ.setQuestionType("jeopardy");
        jeopardyQ.setIsMandatory(true);
        jeopardyQ.setDisplayOrder(1);
        jeopardyQ.setMaxWager(50);

        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(roundRepository.findBySeasonIdAndStatus(1, "open")).thenReturn(List.of(openRound));
        when(responseRepository.findByParticipantIdAndRoundId(1, 1)).thenReturn(Optional.empty());
        when(questionRepository.findByRoundIdOrderByDisplayOrder(1)).thenReturn(List.of(jeopardyQ));
        when(roundRepository.findByDisplayWithRoundId(1)).thenReturn(Collections.emptyList());

        PickAnswerDto answer = new PickAnswerDto();
        answer.setQuestionId(2);
        answer.setSelectedOptionId(1);
        answer.setFreeFormValue("-5");

        SubmitPicksDto dto = new SubmitPicksDto();
        dto.setParticipantId(1);
        dto.setAnswers(List.of(answer));

        assertThrows(IllegalArgumentException.class, () -> service.submitPicks(dto));
    }

    @Test
    void submitPicks_mandatoryFreeForm_empty_throwsException() {
        Question freeFormQ = new Question();
        freeFormQ.setId(3);
        freeFormQ.setRound(openRound);
        freeFormQ.setTitle("Free form Q");
        freeFormQ.setQuestionType("free_form");
        freeFormQ.setIsMandatory(true);
        freeFormQ.setDisplayOrder(1);

        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(roundRepository.findBySeasonIdAndStatus(1, "open")).thenReturn(List.of(openRound));
        when(responseRepository.findByParticipantIdAndRoundId(1, 1)).thenReturn(Optional.empty());
        when(questionRepository.findByRoundIdOrderByDisplayOrder(1)).thenReturn(List.of(freeFormQ));
        when(roundRepository.findByDisplayWithRoundId(1)).thenReturn(Collections.emptyList());

        PickAnswerDto answer = new PickAnswerDto();
        answer.setQuestionId(3);
        answer.setFreeFormValue("   "); // Blank

        SubmitPicksDto dto = new SubmitPicksDto();
        dto.setParticipantId(1);
        dto.setAnswers(List.of(answer));

        assertThrows(IllegalArgumentException.class, () -> service.submitPicks(dto));
    }

    @Test
    void submitPicks_nonExistentParticipant_throwsException() {
        when(participantRepository.findById(999)).thenReturn(Optional.empty());

        SubmitPicksDto dto = new SubmitPicksDto();
        dto.setParticipantId(999);
        dto.setAnswers(Collections.emptyList());

        assertThrows(NoSuchElementException.class, () -> service.submitPicks(dto));
    }

    @Test
    void submitPicks_numberOfGames_savesAsFreeForm() {
        Question nogQ = new Question();
        nogQ.setId(5);
        nogQ.setRound(openRound);
        nogQ.setTitle("Number of games");
        nogQ.setQuestionType("number_of_games");
        nogQ.setIsMandatory(true);
        nogQ.setDisplayOrder(1);

        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(roundRepository.findBySeasonIdAndStatus(1, "open")).thenReturn(List.of(openRound));
        when(responseRepository.findByParticipantIdAndRoundId(1, 1)).thenReturn(Optional.empty());
        when(questionRepository.findByRoundIdOrderByDisplayOrder(1)).thenReturn(List.of(nogQ));
        when(roundRepository.findByDisplayWithRoundId(1)).thenReturn(Collections.emptyList());
        when(responseRepository.save(any(Response.class))).thenAnswer(inv -> {
            Response r = inv.getArgument(0);
            r.setId(1);
            return r;
        });
        when(responseAnswerRepository.saveAll(any())).thenReturn(Collections.emptyList());

        PickAnswerDto answer = new PickAnswerDto();
        answer.setQuestionId(5);
        answer.setFreeFormValue("6");

        SubmitPicksDto dto = new SubmitPicksDto();
        dto.setParticipantId(1);
        dto.setAnswers(List.of(answer));

        assertDoesNotThrow(() -> service.submitPicks(dto));
        verify(responseRepository).save(any(Response.class));
    }

    @Test
    void submitPicks_mandatoryNumberOfGames_unanswered_throwsException() {
        Question nogQ = new Question();
        nogQ.setId(5);
        nogQ.setRound(openRound);
        nogQ.setTitle("Number of games");
        nogQ.setQuestionType("number_of_games");
        nogQ.setIsMandatory(true);
        nogQ.setDisplayOrder(1);

        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(roundRepository.findBySeasonIdAndStatus(1, "open")).thenReturn(List.of(openRound));
        when(responseRepository.findByParticipantIdAndRoundId(1, 1)).thenReturn(Optional.empty());
        when(questionRepository.findByRoundIdOrderByDisplayOrder(1)).thenReturn(List.of(nogQ));
        when(roundRepository.findByDisplayWithRoundId(1)).thenReturn(Collections.emptyList());

        SubmitPicksDto dto = new SubmitPicksDto();
        dto.setParticipantId(1);
        dto.setAnswers(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> service.submitPicks(dto));
    }
}
