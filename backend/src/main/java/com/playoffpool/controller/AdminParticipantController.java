package com.playoffpool.controller;

import com.playoffpool.dto.ParticipantResponseDto;
import com.playoffpool.model.Participant;
import com.playoffpool.service.AdminParticipantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminParticipantController {

    private final AdminParticipantService adminParticipantService;

    public AdminParticipantController(AdminParticipantService adminParticipantService) {
        this.adminParticipantService = adminParticipantService;
    }

    @GetMapping("/participants")
    public List<Participant> getAllParticipants(@RequestParam(required = false) Integer seasonId) {
        if (seasonId != null) {
            return adminParticipantService.getParticipantsBySeason(seasonId);
        }
        return adminParticipantService.getAllParticipants();
    }

    @PutMapping("/participants/{id}")
    public Participant updateParticipant(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        return adminParticipantService.updateParticipant(id,
                body.get("name"), body.get("email"), body.get("teamName"), body.get("division"));
    }

    @PatchMapping("/participants/{id}/paid")
    public Participant updatePaidStatus(@PathVariable Integer id, @RequestBody Map<String, Boolean> body) {
        return adminParticipantService.updatePaidStatus(id, body.get("paid"));
    }

    @GetMapping("/rounds/{roundId}/responses")
    public List<ParticipantResponseDto> getResponsesByRound(@PathVariable Integer roundId) {
        return adminParticipantService.getResponsesByRound(roundId);
    }

    @GetMapping("/participants/{participantId}/responses")
    public List<ParticipantResponseDto> getResponsesByParticipant(@PathVariable Integer participantId) {
        return adminParticipantService.getResponsesByParticipant(participantId);
    }
}
