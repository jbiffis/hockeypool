package com.playoffpool.controller;

import com.playoffpool.dto.PoolFormDto;
import com.playoffpool.dto.RoundDto;
import com.playoffpool.model.Participant;
import com.playoffpool.model.Season;
import com.playoffpool.service.PoolFormService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PoolFormController.class)
@AutoConfigureMockMvc(addFilters = false)
class PoolFormControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PoolFormService poolFormService;

    private Participant participant;

    @BeforeEach
    void setUp() {
        Season season = new Season();
        season.setId(1);

        participant = new Participant();
        participant.setId(1);
        participant.setName("Test Player");
        participant.setEmail("test@example.com");
        participant.setTeamName("Team Alpha");
        participant.setSeason(season);
    }

    @Test
    void lookup_existingParticipant_returnsParticipant() throws Exception {
        when(poolFormService.findParticipantByEmailAndSeason("test@example.com", 1))
                .thenReturn(Optional.of(participant));

        mockMvc.perform(post("/api/pool/lookup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"seasonId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Player"))
                .andExpect(jsonPath("$.returning").value(true));
    }

    @Test
    void lookup_nonExistingParticipant_returns404() throws Exception {
        when(poolFormService.findParticipantByEmailAndSeason("unknown@example.com", 1))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/pool/lookup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"unknown@example.com\",\"seasonId\":1}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void lookup_withoutSeasonId_usesEmailOnly() throws Exception {
        when(poolFormService.findParticipantByEmail("test@example.com"))
                .thenReturn(Optional.of(participant));

        mockMvc.perform(post("/api/pool/lookup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Player"));
    }

    @Test
    void register_newParticipant_returnsCreated() throws Exception {
        when(poolFormService.registerParticipant("new@example.com", "New Player", "Team New", 1))
                .thenReturn(participant);

        mockMvc.perform(post("/api/pool/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"new@example.com\",\"name\":\"New Player\",\"teamName\":\"Team New\",\"seasonId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Player"));
    }

    @Test
    void register_duplicateEmail_returns400() throws Exception {
        when(poolFormService.registerParticipant("test@example.com", "Name", "Team", 1))
                .thenThrow(new IllegalArgumentException("Email is already registered for this season"));

        mockMvc.perform(post("/api/pool/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"name\":\"Name\",\"teamName\":\"Team\",\"seasonId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email is already registered for this season"));
    }

    @Test
    void getForm_returnsFormDto() throws Exception {
        PoolFormDto formDto = new PoolFormDto();
        RoundDto roundDto = new RoundDto();
        roundDto.setId(1);
        roundDto.setName("Round 1");
        formDto.setRound(roundDto);
        formDto.setQuestions(Collections.emptyList());
        formDto.setAlreadySubmitted(false);

        when(poolFormService.getRoundForm(1, null, null)).thenReturn(formDto);

        mockMvc.perform(get("/api/pool/form").param("participantId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.round.name").value("Round 1"))
                .andExpect(jsonPath("$.alreadySubmitted").value(false));
    }

    @Test
    void getForm_noOpenRound_returns404() throws Exception {
        when(poolFormService.getRoundForm(null, null, null))
                .thenThrow(new NoSuchElementException("No round is currently open"));

        mockMvc.perform(get("/api/pool/form"))
                .andExpect(status().isNotFound());
    }

    @Test
    void submitPicks_success_returnsMessage() throws Exception {
        doNothing().when(poolFormService).submitPicks(any());

        mockMvc.perform(post("/api/pool/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"participantId\":1,\"answers\":[{\"questionId\":1,\"selectedOptionIds\":[1,2]}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Picks submitted successfully"));
    }

    @Test
    void submitPicks_pastDeadline_returns400() throws Exception {
        doThrow(new IllegalArgumentException("The deadline for this round has passed"))
                .when(poolFormService).submitPicks(any());

        mockMvc.perform(post("/api/pool/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"participantId\":1,\"answers\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("The deadline for this round has passed"));
    }

    @Test
    void submitPicks_alreadySubmitted_returns400() throws Exception {
        doThrow(new IllegalArgumentException("You have already submitted picks for this round"))
                .when(poolFormService).submitPicks(any());

        mockMvc.perform(post("/api/pool/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"participantId\":1,\"answers\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("You have already submitted picks for this round"));
    }
}
