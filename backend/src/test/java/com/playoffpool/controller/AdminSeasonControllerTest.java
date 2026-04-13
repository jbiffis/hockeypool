package com.playoffpool.controller;

import com.playoffpool.model.Season;
import com.playoffpool.service.AdminSeasonService;
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

@WebMvcTest(AdminSeasonController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminSeasonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminSeasonService adminSeasonService;

    private Season season;

    @BeforeEach
    void setUp() {
        season = new Season();
        season.setId(1);
        season.setName("2025 Playoffs");
        season.setYear(2025);
        season.setStatus("active");
        season.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));
    }

    @Test
    void getAllSeasons_returnsListOfSeasons() throws Exception {
        when(adminSeasonService.getAllSeasons()).thenReturn(List.of(season));

        mockMvc.perform(get("/api/admin/seasons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("2025 Playoffs"))
                .andExpect(jsonPath("$[0].year").value(2025))
                .andExpect(jsonPath("$[0].status").value("active"));
    }

    @Test
    void getSeason_existingId_returnsSeason() throws Exception {
        when(adminSeasonService.getSeason(1)).thenReturn(season);

        mockMvc.perform(get("/api/admin/seasons/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("2025 Playoffs"));
    }

    @Test
    void getSeason_nonExistingId_returns404() throws Exception {
        when(adminSeasonService.getSeason(999)).thenThrow(new NoSuchElementException("Season not found with id: 999"));

        mockMvc.perform(get("/api/admin/seasons/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Season not found with id: 999"));
    }

    @Test
    void createSeason_returnsCreatedSeason() throws Exception {
        when(adminSeasonService.createSeason(any())).thenReturn(season);

        mockMvc.perform(post("/api/admin/seasons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"2025 Playoffs\",\"year\":2025,\"status\":\"active\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("2025 Playoffs"));
    }

    @Test
    void updateSeason_returnsUpdatedSeason() throws Exception {
        season.setName("Updated Season");
        when(adminSeasonService.updateSeason(eq(1), any())).thenReturn(season);

        mockMvc.perform(put("/api/admin/seasons/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Season\",\"year\":2025,\"status\":\"active\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Season"));
    }

    @Test
    void deleteSeason_returnsNoContent() throws Exception {
        doNothing().when(adminSeasonService).deleteSeason(1);

        mockMvc.perform(delete("/api/admin/seasons/1"))
                .andExpect(status().isNoContent());

        verify(adminSeasonService).deleteSeason(1);
    }

    @Test
    void deleteSeason_nonExistingId_returns404() throws Exception {
        doThrow(new NoSuchElementException("Season not found with id: 999"))
                .when(adminSeasonService).deleteSeason(999);

        mockMvc.perform(delete("/api/admin/seasons/999"))
                .andExpect(status().isNotFound());
    }
}
