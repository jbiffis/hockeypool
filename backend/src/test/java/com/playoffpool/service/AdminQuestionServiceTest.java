package com.playoffpool.service;

import com.playoffpool.dto.QuestionDto;
import com.playoffpool.dto.QuestionOptionDto;
import com.playoffpool.model.Question;
import com.playoffpool.model.QuestionOption;
import com.playoffpool.model.Round;
import com.playoffpool.model.Season;
import com.playoffpool.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminQuestionServiceTest {

    @Mock private QuestionRepository questionRepository;
    @Mock private QuestionOptionRepository questionOptionRepository;
    @Mock private RoundRepository roundRepository;
    @Mock private ResponseAnswerRepository responseAnswerRepository;
    @Mock private ParticipantScoreRepository participantScoreRepository;

    @InjectMocks
    private AdminQuestionService service;

    private Round round;
    private Question question;

    @BeforeEach
    void setUp() {
        Season season = new Season();
        season.setId(1);

        round = new Round();
        round.setId(1);
        round.setName("Round 1");
        round.setSeason(season);

        question = new Question();
        question.setId(1);
        question.setRound(round);
        question.setTitle("Who will win?");
        question.setQuestionType("multi_select");
        question.setIsMandatory(true);
        question.setDisplayOrder(1);
        question.setMaxSelections(4);
    }

    @Test
    void getQuestionsByRound_returnsQuestionDtos() {
        when(questionRepository.findByRoundIdOrderByDisplayOrder(1)).thenReturn(List.of(question));

        List<QuestionDto> result = service.getQuestionsByRound(1);

        assertEquals(1, result.size());
        assertEquals("Who will win?", result.get(0).getTitle());
        assertEquals(1, result.get(0).getRoundId());
    }

    @Test
    void getQuestion_existingId_returnsDto() {
        when(questionRepository.findById(1)).thenReturn(Optional.of(question));

        QuestionDto result = service.getQuestion(1);

        assertEquals("Who will win?", result.getTitle());
        assertEquals("multi_select", result.getQuestionType());
    }

    @Test
    void getQuestion_nonExistingId_throwsException() {
        when(questionRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getQuestion(999));
    }

    @Test
    void createQuestion_setsAllFields() {
        when(roundRepository.findById(1)).thenReturn(Optional.of(round));
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> {
            Question q = inv.getArgument(0);
            q.setId(2);
            return q;
        });

        QuestionDto dto = new QuestionDto();
        dto.setTitle("New Question");
        dto.setQuestionType("free_form");
        dto.setIsMandatory(false);
        dto.setDisplayOrder(2);
        dto.setDescription("A description");
        dto.setImageUrl("http://example.com/img.png");

        QuestionDto result = service.createQuestion(1, dto);

        assertEquals("New Question", result.getTitle());
        assertEquals("free_form", result.getQuestionType());
        assertEquals(1, result.getRoundId());
    }

    @Test
    void createQuestion_withParentQuestion_setsParent() {
        Question parent = new Question();
        parent.setId(10);
        parent.setRound(round);
        parent.setTitle("Parent");
        parent.setQuestionType("multi_select");
        parent.setDisplayOrder(1);

        when(roundRepository.findById(1)).thenReturn(Optional.of(round));
        when(questionRepository.findById(10)).thenReturn(Optional.of(parent));
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> {
            Question q = inv.getArgument(0);
            q.setId(2);
            return q;
        });

        QuestionDto dto = new QuestionDto();
        dto.setTitle("Child Question");
        dto.setQuestionType("free_form");
        dto.setDisplayOrder(2);
        dto.setParentQuestionId(10);

        QuestionDto result = service.createQuestion(1, dto);

        assertEquals(10, result.getParentQuestionId());
    }

    @Test
    void createQuestion_nonExistingRound_throwsException() {
        when(roundRepository.findById(999)).thenReturn(Optional.empty());

        QuestionDto dto = new QuestionDto();
        dto.setTitle("Test");
        dto.setQuestionType("free_form");
        dto.setDisplayOrder(1);

        assertThrows(NoSuchElementException.class, () -> service.createQuestion(999, dto));
    }

    @Test
    void updateQuestion_changesFields() {
        when(questionRepository.findById(1)).thenReturn(Optional.of(question));
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> inv.getArgument(0));

        QuestionDto dto = new QuestionDto();
        dto.setTitle("Updated Question");
        dto.setQuestionType("jeopardy");
        dto.setIsMandatory(false);
        dto.setDisplayOrder(3);
        dto.setMaxWager(100);

        QuestionDto result = service.updateQuestion(1, dto);

        assertEquals("Updated Question", result.getTitle());
        assertEquals("jeopardy", result.getQuestionType());
    }

    @Test
    void updateQuestion_removesParent_whenNull() {
        Question parent = new Question();
        parent.setId(10);
        question.setParentQuestion(parent);

        when(questionRepository.findById(1)).thenReturn(Optional.of(question));
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> inv.getArgument(0));

        QuestionDto dto = new QuestionDto();
        dto.setTitle("Updated");
        dto.setQuestionType("multi_select");
        dto.setDisplayOrder(1);
        dto.setParentQuestionId(null);

        QuestionDto result = service.updateQuestion(1, dto);

        assertNull(result.getParentQuestionId());
    }

    @Test
    void deleteQuestion_cascadesRelatedData() {
        when(participantScoreRepository.findByQuestionId(1)).thenReturn(Collections.emptyList());
        when(responseAnswerRepository.findByQuestionId(1)).thenReturn(Collections.emptyList());
        when(questionOptionRepository.findByQuestionIdOrderByDisplayOrder(1)).thenReturn(Collections.emptyList());

        service.deleteQuestion(1);

        verify(participantScoreRepository).deleteAll(any());
        verify(responseAnswerRepository).deleteAll(any());
        verify(questionOptionRepository).deleteAll(any());
        verify(questionRepository).deleteById(1);
    }

    @Test
    void getOptionsByQuestion_returnsDtoList() {
        QuestionOption option = new QuestionOption();
        option.setId(1);
        option.setQuestion(question);
        option.setOptionText("Option A");
        option.setDisplayOrder(1);
        option.setPoints(10);

        when(questionOptionRepository.findByQuestionIdOrderByDisplayOrder(1))
                .thenReturn(List.of(option));

        List<QuestionOptionDto> result = service.getOptionsByQuestion(1);

        assertEquals(1, result.size());
        assertEquals("Option A", result.get(0).getOptionText());
        assertEquals(10, result.get(0).getPoints());
    }

    @Test
    void createOption_setsAllFields() {
        when(questionRepository.findById(1)).thenReturn(Optional.of(question));
        when(questionOptionRepository.save(any(QuestionOption.class))).thenAnswer(inv -> {
            QuestionOption o = inv.getArgument(0);
            o.setId(1);
            return o;
        });

        QuestionOptionDto dto = new QuestionOptionDto();
        dto.setOptionText("Option B");
        dto.setDisplayOrder(2);
        dto.setPoints(5);
        dto.setSubtext("A subtext");
        dto.setImageUrl("https://example.com/logo.png");

        QuestionOptionDto result = service.createOption(1, dto);

        assertEquals("Option B", result.getOptionText());
        assertEquals(5, result.getPoints());
        assertEquals("https://example.com/logo.png", result.getImageUrl());
    }

    @Test
    void updateOption_changesFields() {
        QuestionOption option = new QuestionOption();
        option.setId(1);
        option.setQuestion(question);
        option.setOptionText("Old Text");
        option.setDisplayOrder(1);

        when(questionOptionRepository.findById(1)).thenReturn(Optional.of(option));
        when(questionOptionRepository.save(any(QuestionOption.class))).thenAnswer(inv -> inv.getArgument(0));

        QuestionOptionDto dto = new QuestionOptionDto();
        dto.setOptionText("New Text");
        dto.setDisplayOrder(2);
        dto.setPoints(20);
        dto.setImageUrl("https://example.com/updated.png");

        QuestionOptionDto result = service.updateOption(1, dto);

        assertEquals("New Text", result.getOptionText());
        assertEquals(20, result.getPoints());
        assertEquals("https://example.com/updated.png", result.getImageUrl());
    }

    @Test
    void deleteOption_deletesById() {
        service.deleteOption(1);

        verify(questionOptionRepository).deleteById(1);
    }

    @Test
    void updateOptionPoints_updatesPointsOnly() {
        QuestionOption option = new QuestionOption();
        option.setId(1);
        option.setQuestion(question);
        option.setOptionText("Option A");
        option.setDisplayOrder(1);
        option.setPoints(5);

        when(questionOptionRepository.findById(1)).thenReturn(Optional.of(option));
        when(questionOptionRepository.save(any(QuestionOption.class))).thenAnswer(inv -> inv.getArgument(0));

        QuestionOptionDto result = service.updateOptionPoints(1, 25);

        assertEquals(25, result.getPoints());
        assertEquals("Option A", result.getOptionText());
    }

    @Test
    void updateOptionPoints_nonExistingOption_throwsException() {
        when(questionOptionRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.updateOptionPoints(999, 10));
    }
}
