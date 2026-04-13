package com.playoffpool.controller;

import com.playoffpool.dto.QuestionOptionDto;
import com.playoffpool.service.AdminQuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminQuestionOptionController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminQuestionOptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminQuestionService adminQuestionService;

    private QuestionOptionDto optionDto;

    @BeforeEach
    void setUp() {
        optionDto = new QuestionOptionDto();
        optionDto.setId(1);
        optionDto.setQuestionId(1);
        optionDto.setOptionText("Toronto Maple Leafs");
        optionDto.setDisplayOrder(1);
        optionDto.setPoints(10);
    }

    @Test
    void listOptions_returnsOptionList() throws Exception {
        when(adminQuestionService.getOptionsByQuestion(1)).thenReturn(List.of(optionDto));

        mockMvc.perform(get("/api/admin/questions/1/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].optionText").value("Toronto Maple Leafs"))
                .andExpect(jsonPath("$[0].points").value(10));
    }

    @Test
    void createOption_returnsCreatedOption() throws Exception {
        when(adminQuestionService.createOption(eq(1), any())).thenReturn(optionDto);

        mockMvc.perform(post("/api/admin/questions/1/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"optionText\":\"Toronto Maple Leafs\",\"displayOrder\":1,\"points\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.optionText").value("Toronto Maple Leafs"));
    }

    @Test
    void updateOption_returnsUpdatedOption() throws Exception {
        optionDto.setOptionText("Edmonton Oilers");
        when(adminQuestionService.updateOption(eq(1), any())).thenReturn(optionDto);

        mockMvc.perform(put("/api/admin/questions/1/options/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"optionText\":\"Edmonton Oilers\",\"displayOrder\":1,\"points\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.optionText").value("Edmonton Oilers"));
    }

    @Test
    void deleteOption_returnsNoContent() throws Exception {
        doNothing().when(adminQuestionService).deleteOption(1);

        mockMvc.perform(delete("/api/admin/questions/1/options/1"))
                .andExpect(status().isNoContent());

        verify(adminQuestionService).deleteOption(1);
    }

    @Test
    void updatePoints_returnsUpdatedOption() throws Exception {
        optionDto.setPoints(25);
        when(adminQuestionService.updateOptionPoints(1, 25)).thenReturn(optionDto);

        mockMvc.perform(patch("/api/admin/questions/1/options/1/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"points\":25}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points").value(25));
    }

    @Test
    void updatePoints_nonExistingOption_returns404() throws Exception {
        when(adminQuestionService.updateOptionPoints(eq(999), any()))
                .thenThrow(new NoSuchElementException("Option not found with id: 999"));

        mockMvc.perform(patch("/api/admin/questions/1/options/999/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"points\":10}"))
                .andExpect(status().isNotFound());
    }
}
