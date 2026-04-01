package com.playoffpool.controller;

import com.playoffpool.dto.QuestionOptionDto;
import com.playoffpool.service.AdminQuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/questions/{questionId}/options")
public class AdminQuestionOptionController {

    private final AdminQuestionService adminQuestionService;

    public AdminQuestionOptionController(AdminQuestionService adminQuestionService) {
        this.adminQuestionService = adminQuestionService;
    }

    @GetMapping
    public ResponseEntity<List<QuestionOptionDto>> listOptions(@PathVariable Integer questionId) {
        List<QuestionOptionDto> options = adminQuestionService.getOptionsByQuestion(questionId);
        return ResponseEntity.ok(options);
    }

    @PostMapping
    public ResponseEntity<QuestionOptionDto> createOption(@PathVariable Integer questionId,
                                                          @RequestBody QuestionOptionDto dto) {
        QuestionOptionDto created = adminQuestionService.createOption(questionId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionOptionDto> updateOption(@PathVariable Integer questionId,
                                                          @PathVariable Integer id,
                                                          @RequestBody QuestionOptionDto dto) {
        QuestionOptionDto updated = adminQuestionService.updateOption(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOption(@PathVariable Integer questionId,
                                             @PathVariable Integer id) {
        adminQuestionService.deleteOption(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/points")
    public ResponseEntity<QuestionOptionDto> updatePoints(@PathVariable Integer questionId,
                                                          @PathVariable Integer id,
                                                          @RequestBody Map<String, Integer> body) {
        Integer points = body.get("points");
        QuestionOptionDto updated = adminQuestionService.updateOptionPoints(id, points);
        return ResponseEntity.ok(updated);
    }
}
