package com.playoffpool.controller;

import com.playoffpool.dto.QuestionDto;
import com.playoffpool.service.AdminQuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/rounds/{roundId}/questions")
public class AdminQuestionController {

    private final AdminQuestionService adminQuestionService;

    public AdminQuestionController(AdminQuestionService adminQuestionService) {
        this.adminQuestionService = adminQuestionService;
    }

    @GetMapping
    public ResponseEntity<List<QuestionDto>> listQuestions(@PathVariable Integer roundId) {
        List<QuestionDto> questions = adminQuestionService.getQuestionsByRound(roundId);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionDto> getQuestion(@PathVariable Integer roundId,
                                                   @PathVariable Integer id) {
        QuestionDto question = adminQuestionService.getQuestion(id);
        return ResponseEntity.ok(question);
    }

    @PostMapping
    public ResponseEntity<QuestionDto> createQuestion(@PathVariable Integer roundId,
                                                      @RequestBody QuestionDto dto) {
        QuestionDto created = adminQuestionService.createQuestion(roundId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionDto> updateQuestion(@PathVariable Integer roundId,
                                                      @PathVariable Integer id,
                                                      @RequestBody QuestionDto dto) {
        QuestionDto updated = adminQuestionService.updateQuestion(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Integer roundId,
                                               @PathVariable Integer id) {
        adminQuestionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
