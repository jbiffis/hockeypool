package com.playoffpool.controller;

import com.playoffpool.dto.ParticipantDto;
import com.playoffpool.dto.PoolFormDto;
import com.playoffpool.dto.SubmitPicksDto;
import com.playoffpool.model.Participant;
import com.playoffpool.service.PoolFormService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pool")
public class PoolFormController {

    private final PoolFormService poolFormService;

    public PoolFormController(PoolFormService poolFormService) {
        this.poolFormService = poolFormService;
    }

    @PostMapping("/lookup")
    public ResponseEntity<ParticipantDto> lookup(@RequestBody Map<String, Object> body) {
        String email = (String) body.get("email");
        Integer seasonId = body.get("seasonId") != null ? ((Number) body.get("seasonId")).intValue() : null;
        Optional<Participant> participant;
        if (seasonId != null) {
            participant = poolFormService.findParticipantByEmailAndSeason(email, seasonId);
        } else {
            participant = poolFormService.findParticipantByEmail(email);
        }
        if (participant.isPresent()) {
            return ResponseEntity.ok(ParticipantDto.fromEntity(participant.get(), true));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping("/register")
    public ResponseEntity<ParticipantDto> register(@RequestBody Map<String, Object> body) {
        String email = (String) body.get("email");
        String name = (String) body.get("name");
        String teamName = (String) body.get("teamName");
        Integer seasonId = body.get("seasonId") != null ? ((Number) body.get("seasonId")).intValue() : null;
        Participant participant = poolFormService.registerParticipant(email, name, teamName, seasonId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ParticipantDto.fromEntity(participant, false));
    }

    @GetMapping("/form")
    public ResponseEntity<PoolFormDto> getForm(@RequestParam(required = false) Integer participantId,
                                                @RequestParam(required = false) Integer roundId,
                                                @RequestParam(required = false) Integer seasonId) {
        PoolFormDto form = poolFormService.getRoundForm(participantId, roundId, seasonId);
        return ResponseEntity.ok(form);
    }

    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submitPicks(@RequestBody SubmitPicksDto dto) {
        poolFormService.submitPicks(dto);
        return ResponseEntity.ok(Map.of("message", "Picks submitted successfully"));
    }
}
