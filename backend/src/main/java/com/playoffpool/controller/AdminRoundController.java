package com.playoffpool.controller;

import com.playoffpool.dto.RoundDto;
import com.playoffpool.service.AdminRoundService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/rounds")
public class AdminRoundController {

    private final AdminRoundService adminRoundService;

    public AdminRoundController(AdminRoundService adminRoundService) {
        this.adminRoundService = adminRoundService;
    }

    @GetMapping
    public ResponseEntity<List<RoundDto>> getAllRounds(@RequestParam(required = false) Integer seasonId) {
        List<RoundDto> rounds;
        if (seasonId != null) {
            rounds = adminRoundService.getRoundsBySeason(seasonId).stream()
                    .map(RoundDto::fromEntity)
                    .collect(Collectors.toList());
        } else {
            rounds = adminRoundService.getAllRounds().stream()
                    .map(RoundDto::fromEntity)
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(rounds);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoundDto> getRound(@PathVariable Integer id) {
        return ResponseEntity.ok(RoundDto.fromEntity(adminRoundService.getRound(id)));
    }

    @PostMapping
    public ResponseEntity<RoundDto> createRound(@RequestBody RoundDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RoundDto.fromEntity(adminRoundService.createRound(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoundDto> updateRound(@PathVariable Integer id, @RequestBody RoundDto dto) {
        return ResponseEntity.ok(RoundDto.fromEntity(adminRoundService.updateRound(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRound(@PathVariable Integer id) {
        adminRoundService.deleteRound(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RoundDto> updateRoundStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        return ResponseEntity.ok(RoundDto.fromEntity(adminRoundService.updateRoundStatus(id, newStatus)));
    }
}
