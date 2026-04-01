package com.playoffpool.controller;

import com.playoffpool.dto.SeasonDto;
import com.playoffpool.service.AdminSeasonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/seasons")
public class AdminSeasonController {

    private final AdminSeasonService adminSeasonService;

    public AdminSeasonController(AdminSeasonService adminSeasonService) {
        this.adminSeasonService = adminSeasonService;
    }

    @GetMapping
    public ResponseEntity<List<SeasonDto>> getAllSeasons() {
        List<SeasonDto> seasons = adminSeasonService.getAllSeasons().stream()
                .map(SeasonDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(seasons);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeasonDto> getSeason(@PathVariable Integer id) {
        return ResponseEntity.ok(SeasonDto.fromEntity(adminSeasonService.getSeason(id)));
    }

    @PostMapping
    public ResponseEntity<SeasonDto> createSeason(@RequestBody SeasonDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SeasonDto.fromEntity(adminSeasonService.createSeason(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SeasonDto> updateSeason(@PathVariable Integer id, @RequestBody SeasonDto dto) {
        return ResponseEntity.ok(SeasonDto.fromEntity(adminSeasonService.updateSeason(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeason(@PathVariable Integer id) {
        adminSeasonService.deleteSeason(id);
        return ResponseEntity.noContent().build();
    }
}
