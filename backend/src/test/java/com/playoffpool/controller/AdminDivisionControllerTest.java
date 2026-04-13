package com.playoffpool.controller;

import com.playoffpool.model.Division;
import com.playoffpool.model.Season;
import com.playoffpool.service.AdminDivisionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminDivisionController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminDivisionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminDivisionService adminDivisionService;

    private Division division;

    @BeforeEach
    void setUp() {
        Season season = new Season();
        season.setId(1);

        division = new Division();
        division.setId(1);
        division.setName("East");
        division.setSeason(season);
        division.setParticipants(new HashSet<>());
    }

    @Test
    void getDivisions_returnsList() throws Exception {
        when(adminDivisionService.getDivisionsForSeason(1)).thenReturn(List.of(division));

        mockMvc.perform(get("/api/admin/divisions").param("seasonId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("East"));
    }

    @Test
    void createDivision_returnsCreated() throws Exception {
        when(adminDivisionService.createDivision(eq(1), eq("East"))).thenReturn(division);

        mockMvc.perform(post("/api/admin/divisions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seasonId\":1,\"name\":\"East\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("East"));
    }

    @Test
    void addParticipant_returnsNoContent() throws Exception {
        doNothing().when(adminDivisionService).addParticipantToDivision(1, 1);

        mockMvc.perform(post("/api/admin/divisions/1/participants/1"))
                .andExpect(status().isNoContent());

        verify(adminDivisionService).addParticipantToDivision(1, 1);
    }

    @Test
    void removeParticipant_returnsNoContent() throws Exception {
        doNothing().when(adminDivisionService).removeParticipantFromDivision(1, 1);

        mockMvc.perform(delete("/api/admin/divisions/1/participants/1"))
                .andExpect(status().isNoContent());

        verify(adminDivisionService).removeParticipantFromDivision(1, 1);
    }
}
