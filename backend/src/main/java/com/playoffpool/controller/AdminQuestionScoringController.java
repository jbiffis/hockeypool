package com.playoffpool.controller;

import com.playoffpool.service.AdminQuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/questions/{questionId}")
public class AdminQuestionScoringController {

    private final AdminQuestionService adminQuestionService;

    public AdminQuestionScoringController(AdminQuestionService adminQuestionService) {
        this.adminQuestionService = adminQuestionService;
    }

    @PostMapping("/score-best-team-name")
    public ResponseEntity<Map<String, Object>> scoreBestTeamName(@PathVariable Integer questionId) {
        Map<String, Object> result = adminQuestionService.scoreBestTeamName(questionId);
        return ResponseEntity.ok(result);
    }
}
