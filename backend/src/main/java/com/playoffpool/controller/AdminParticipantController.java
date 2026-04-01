package com.playoffpool.controller;

import com.playoffpool.dto.ParticipantResponseDto;
import com.playoffpool.model.Participant;
import com.playoffpool.service.AdminParticipantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/rounds/{roundId}/responses")
    public List<ParticipantResponseDto> getResponsesByRound(@PathVariable Integer roundId) {
        return adminParticipantService.getResponsesByRound(roundId);
    }

    @GetMapping("/participants/{participantId}/responses")
    public List<ParticipantResponseDto> getResponsesByParticipant(@PathVariable Integer participantId) {
        return adminParticipantService.getResponsesByParticipant(participantId);
    }
}
