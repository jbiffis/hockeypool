package com.playoffpool.controller;

import com.playoffpool.dto.DivisionDto;
import com.playoffpool.service.AdminDivisionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/divisions")
public class AdminDivisionController {

    private final AdminDivisionService adminDivisionService;

    public AdminDivisionController(AdminDivisionService adminDivisionService) {
        this.adminDivisionService = adminDivisionService;
    }

    @GetMapping
    public ResponseEntity<List<DivisionDto>> getDivisions(@RequestParam Integer seasonId) {
        List<DivisionDto> dtos = adminDivisionService.getDivisionsForSeason(seasonId).stream()
            .map(DivisionDto::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<DivisionDto> createDivision(@RequestBody Map<String, Object> body) {
        Integer seasonId = (Integer) body.get("seasonId");
        String name = (String) body.get("name");
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(DivisionDto.from(adminDivisionService.createDivision(seasonId, name)));
    }

    @PostMapping("/{divisionId}/participants/{participantId}")
    public ResponseEntity<Void> addParticipant(@PathVariable Integer divisionId,
                                               @PathVariable Integer participantId) {
        adminDivisionService.addParticipantToDivision(divisionId, participantId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{divisionId}/participants/{participantId}")
    public ResponseEntity<Void> removeParticipant(@PathVariable Integer divisionId,
                                                  @PathVariable Integer participantId) {
        adminDivisionService.removeParticipantFromDivision(divisionId, participantId);
        return ResponseEntity.noContent().build();
    }
}
