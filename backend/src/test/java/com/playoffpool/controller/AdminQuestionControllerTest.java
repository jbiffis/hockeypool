package com.playoffpool.controller;

import com.playoffpool.dto.QuestionDto;
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

@WebMvcTest(AdminQuestionController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminQuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminQuestionService adminQuestionService;

    private QuestionDto questionDto;

    @BeforeEach
    void setUp() {
        questionDto = new QuestionDto();
        questionDto.setId(1);
        questionDto.setRoundId(1);
        questionDto.setTitle("Who will win the Stanley Cup?");
        questionDto.setQuestionType("multi_select");
        questionDto.setIsMandatory(true);
        questionDto.setDisplayOrder(1);
        questionDto.setMaxSelections(4);
    }

    @Test
    void listQuestions_returnsQuestionList() throws Exception {
        when(adminQuestionService.getQuestionsByRound(1)).thenReturn(List.of(questionDto));

        mockMvc.perform(get("/api/admin/rounds/1/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Who will win the Stanley Cup?"))
                .andExpect(jsonPath("$[0].questionType").value("multi_select"));
    }

    @Test
    void getQuestion_existingId_returnsQuestion() throws Exception {
        when(adminQuestionService.getQuestion(1)).thenReturn(questionDto);

        mockMvc.perform(get("/api/admin/rounds/1/questions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Who will win the Stanley Cup?"));
    }

    @Test
    void getQuestion_nonExistingId_returns404() throws Exception {
        when(adminQuestionService.getQuestion(999))
                .thenThrow(new NoSuchElementException("Question not found with id: 999"));

        mockMvc.perform(get("/api/admin/rounds/1/questions/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createQuestion_returnsCreatedQuestion() throws Exception {
        when(adminQuestionService.createQuestion(eq(1), any())).thenReturn(questionDto);

        mockMvc.perform(post("/api/admin/rounds/1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Who will win the Stanley Cup?\",\"questionType\":\"multi_select\",\"isMandatory\":true,\"displayOrder\":1,\"maxSelections\":4}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Who will win the Stanley Cup?"));
    }

    @Test
    void updateQuestion_returnsUpdatedQuestion() throws Exception {
        questionDto.setTitle("Updated Question");
        when(adminQuestionService.updateQuestion(eq(1), any())).thenReturn(questionDto);

        mockMvc.perform(put("/api/admin/rounds/1/questions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated Question\",\"questionType\":\"multi_select\",\"displayOrder\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Question"));
    }

    @Test
    void deleteQuestion_returnsNoContent() throws Exception {
        doNothing().when(adminQuestionService).deleteQuestion(1);

        mockMvc.perform(delete("/api/admin/rounds/1/questions/1"))
                .andExpect(status().isNoContent());

        verify(adminQuestionService).deleteQuestion(1);
    }
}
