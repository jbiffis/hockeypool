package com.playoffpool.controller;

import com.playoffpool.model.Round;
import com.playoffpool.model.Season;
import com.playoffpool.service.AdminRoundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminRoundController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminRoundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminRoundService adminRoundService;

    private Round round;

    @BeforeEach
    void setUp() {
        Season season = new Season();
        season.setId(1);

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
    void getAllRounds_noFilter_returnsAll() throws Exception {
        when(adminRoundService.getAllRounds()).thenReturn(List.of(round));

        mockMvc.perform(get("/api/admin/rounds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Round 1"))
                .andExpect(jsonPath("$[0].status").value("draft"));
    }

    @Test
    void getAllRounds_withSeasonFilter_returnsFiltered() throws Exception {
        when(adminRoundService.getRoundsBySeason(1)).thenReturn(List.of(round));

        mockMvc.perform(get("/api/admin/rounds").param("seasonId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seasonId").value(1));
    }

    @Test
    void getRound_existingId_returnsRound() throws Exception {
        when(adminRoundService.getRound(1)).thenReturn(round);

        mockMvc.perform(get("/api/admin/rounds/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Round 1"));
    }

    @Test
    void getRound_nonExistingId_returns404() throws Exception {
        when(adminRoundService.getRound(999)).thenThrow(new NoSuchElementException("Round not found"));

        mockMvc.perform(get("/api/admin/rounds/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createRound_returnsCreatedRound() throws Exception {
        when(adminRoundService.createRound(any())).thenReturn(round);

        mockMvc.perform(post("/api/admin/rounds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Round 1\",\"seasonId\":1,\"status\":\"draft\",\"displayOrder\":1,\"deadline\":\"2025-04-15T18:00:00\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Round 1"));
    }

    @Test
    void updateRound_returnsUpdatedRound() throws Exception {
        round.setName("Updated Round");
        when(adminRoundService.updateRound(eq(1), any())).thenReturn(round);

        mockMvc.perform(put("/api/admin/rounds/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Round\",\"status\":\"open\",\"displayOrder\":1,\"deadline\":\"2025-04-15T18:00:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Round"));
    }

    @Test
    void deleteRound_returnsNoContent() throws Exception {
        doNothing().when(adminRoundService).deleteRound(1);

        mockMvc.perform(delete("/api/admin/rounds/1"))
                .andExpect(status().isNoContent());

        verify(adminRoundService).deleteRound(1);
    }

    @Test
    void updateRoundStatus_validStatus_returnsUpdatedRound() throws Exception {
        round.setStatus("open");
        when(adminRoundService.updateRoundStatus(1, "open")).thenReturn(round);

        mockMvc.perform(patch("/api/admin/rounds/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"open\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("open"));
    }

    @Test
    void updateRoundStatus_invalidStatus_returns400() throws Exception {
        when(adminRoundService.updateRoundStatus(1, "invalid"))
                .thenThrow(new IllegalArgumentException("Invalid status: 'invalid'"));

        mockMvc.perform(patch("/api/admin/rounds/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"invalid\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid status: 'invalid'"));
    }
}
