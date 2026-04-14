package com.playoffpool.controller;

import com.playoffpool.dto.ParticipantResponseDto;
import com.playoffpool.model.Participant;
import com.playoffpool.model.Season;
import com.playoffpool.service.AdminParticipantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;

@WebMvcTest(AdminParticipantController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminParticipantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminParticipantService adminParticipantService;

    private Participant participant;

    @BeforeEach
    void setUp() {
        Season season = new Season();
        season.setId(1);
        season.setName("2025 Playoffs");
        season.setYear(2025);
        season.setStatus("active");

        participant = new Participant();
        participant.setId(1);
        participant.setName("Test Player");
        participant.setEmail("test@example.com");
        participant.setTeamName("Team Alpha");
        participant.setSeason(season);
        participant.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getAllParticipants_noFilter_returnsAll() throws Exception {
        when(adminParticipantService.getAllParticipants()).thenReturn(List.of(participant));

        mockMvc.perform(get("/api/admin/participants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Player"));
    }

    @Test
    void getAllParticipants_withSeasonFilter_returnsFiltered() throws Exception {
        when(adminParticipantService.getParticipantsBySeason(1)).thenReturn(List.of(participant));

        mockMvc.perform(get("/api/admin/participants").param("seasonId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void updatePaidStatus_setsParticipantPaid() throws Exception {
        participant.setPaid(true);
        when(adminParticipantService.updatePaidStatus(eq(1), eq(true))).thenReturn(participant);

        mockMvc.perform(patch("/api/admin/participants/1/paid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paid\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.paid").value(true));
    }

    @Test
    void getResponsesByRound_returnsResponseList() throws Exception {
        ParticipantResponseDto responseDto = new ParticipantResponseDto();
        responseDto.setParticipantId(1);
        responseDto.setParticipantName("Test Player");
        responseDto.setRoundId(1);
        responseDto.setRoundName("Round 1");
        responseDto.setAnswers(Collections.emptyList());

        when(adminParticipantService.getResponsesByRound(1)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/api/admin/rounds/1/responses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].participantName").value("Test Player"))
                .andExpect(jsonPath("$[0].roundName").value("Round 1"));
    }

    @Test
    void getResponsesByParticipant_returnsResponseList() throws Exception {
        ParticipantResponseDto responseDto = new ParticipantResponseDto();
        responseDto.setParticipantId(1);
        responseDto.setParticipantName("Test Player");
        responseDto.setRoundId(1);
        responseDto.setRoundName("Round 1");
        responseDto.setAnswers(Collections.emptyList());

        when(adminParticipantService.getResponsesByParticipant(1)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/api/admin/participants/1/responses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].participantId").value(1));
    }
}
