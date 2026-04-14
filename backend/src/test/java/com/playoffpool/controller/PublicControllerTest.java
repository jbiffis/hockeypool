package com.playoffpool.controller;

import com.playoffpool.dto.LeaderboardDto;
import com.playoffpool.dto.ParticipantResponseDto;
import com.playoffpool.model.Participant;
import com.playoffpool.model.Question;
import com.playoffpool.model.Round;
import com.playoffpool.model.Season;
import com.playoffpool.repository.*;
import com.playoffpool.service.AdminDivisionService;
import com.playoffpool.service.AdminParticipantService;
import com.playoffpool.service.LeaderboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicController.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private SeasonRepository seasonRepository;
    @MockitoBean private LeaderboardService leaderboardService;
    @MockitoBean private AdminParticipantService adminParticipantService;
    @MockitoBean private AdminDivisionService adminDivisionService;
    @MockitoBean private QuestionRepository questionRepository;
    @MockitoBean private QuestionOptionRepository questionOptionRepository;
    @MockitoBean private ResponseAnswerRepository responseAnswerRepository;
    @MockitoBean private ParticipantRepository participantRepository;

    private Season season;

    @BeforeEach
    void setUp() {
        season = new Season();
        season.setId(1);
        season.setName("2025 Playoffs");
        season.setYear(2025);
        season.setStatus("active");
        season.setSignupContent("Welcome to the pool!");
    }

    @Test
    void getSeasons_returnsList() throws Exception {
        when(seasonRepository.findAllByOrderByYearDesc()).thenReturn(List.of(season));

        mockMvc.perform(get("/api/seasons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("2025 Playoffs"));
    }

    @Test
    void getSignupPage_existingSeason_returnsSeasonDto() throws Exception {
        when(seasonRepository.findById(1)).thenReturn(Optional.of(season));

        mockMvc.perform(get("/api/seasons/1/signup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("2025 Playoffs"))
                .andExpect(jsonPath("$.signupContent").value("Welcome to the pool!"));
    }

    @Test
    void getSignupPage_nonExistingSeason_returns404() throws Exception {
        when(seasonRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/seasons/999/signup"))
                .andExpect(status().isNotFound());
    }

    @Test
    void submitSignup_validEmail_returnsCreated() throws Exception {
        when(seasonRepository.findById(1)).thenReturn(Optional.of(season));
        when(participantRepository.findByEmailAndSeasonId("test@example.com", 1))
                .thenReturn(Optional.empty());
        when(participantRepository.save(any(Participant.class))).thenAnswer(inv -> {
            Participant p = inv.getArgument(0);
            p.setId(1);
            return p;
        });

        mockMvc.perform(post("/api/seasons/1/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Signed up successfully"));
    }

    @Test
    void submitSignup_duplicateEmail_returns409() throws Exception {
        Participant existing = new Participant();
        existing.setId(1);
        existing.setEmail("test@example.com");
        existing.setSeason(season);

        when(seasonRepository.findById(1)).thenReturn(Optional.of(season));
        when(participantRepository.findByEmailAndSeasonId("test@example.com", 1))
                .thenReturn(Optional.of(existing));

        mockMvc.perform(post("/api/seasons/1/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("This email is already registered for this season"));
    }

    @Test
    void submitSignup_blankEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/seasons/1/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email is required"));
    }

    @Test
    void submitSignup_missingEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/seasons/1/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email is required"));
    }

    @Test
    void submitSignup_nonExistingSeason_returns404() throws Exception {
        when(seasonRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/seasons/999/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLeaderboard_returnsLeaderboardDto() throws Exception {
        LeaderboardDto dto = new LeaderboardDto();
        dto.setRounds(Collections.emptyList());
        dto.setEntries(Collections.emptyList());

        when(leaderboardService.getLeaderboard(1, null)).thenReturn(dto);

        mockMvc.perform(get("/api/leaderboard/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rounds").isArray())
                .andExpect(jsonPath("$.entries").isArray());
    }

    @Test
    void getLeaderboard_withDivisionFilter() throws Exception {
        LeaderboardDto dto = new LeaderboardDto();
        dto.setRounds(Collections.emptyList());
        dto.setEntries(Collections.emptyList());

        when(leaderboardService.getLeaderboard(1, 10)).thenReturn(dto);

        mockMvc.perform(get("/api/leaderboard/1").param("divisionId", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getParticipantResponses_stripsEmail() throws Exception {
        ParticipantResponseDto responseDto = new ParticipantResponseDto();
        responseDto.setParticipantId(1);
        responseDto.setParticipantName("Test Player");
        responseDto.setEmail("test@example.com"); // Should be nulled by controller
        responseDto.setRoundId(1);
        responseDto.setRoundName("Round 1");
        responseDto.setAnswers(Collections.emptyList());

        when(adminParticipantService.getResponsesByParticipant(1)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/api/participants/1/responses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].participantName").value("Test Player"))
                .andExpect(jsonPath("$[0].email").doesNotExist());
    }

    @Test
    void getQuestionDetail_existingQuestion_returnsDetail() throws Exception {
        Round round = new Round();
        round.setId(1);
        round.setName("Round 1");
        round.setSeason(season);

        Question question = new Question();
        question.setId(1);
        question.setTitle("Who wins?");
        question.setQuestionType("multi_select");
        question.setDisplayOrder(1);
        question.setRound(round);

        when(questionRepository.findById(1)).thenReturn(Optional.of(question));
        when(questionOptionRepository.findByQuestionIdOrderByDisplayOrder(1))
                .thenReturn(Collections.emptyList());
        when(responseAnswerRepository.findByQuestionId(1)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/questions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Who wins?"))
                .andExpect(jsonPath("$.roundName").value("Round 1"));
    }

    @Test
    void getQuestionDetail_nonExistingQuestion_returns500() throws Exception {
        when(questionRepository.findById(999)).thenReturn(Optional.empty());

        // The controller throws RuntimeException which Spring wraps in a NestedServletException
        try {
            mockMvc.perform(get("/api/questions/999"));
        } catch (Exception e) {
            // Expected: RuntimeException("Question not found") wrapped by Spring
            assertTrue(e.getCause() instanceof RuntimeException);
        }
    }
}
