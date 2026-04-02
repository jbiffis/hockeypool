package com.playoffpool.service;

import com.playoffpool.model.Division;
import com.playoffpool.model.Participant;
import com.playoffpool.model.Season;
import com.playoffpool.repository.DivisionRepository;
import com.playoffpool.repository.ParticipantRepository;
import com.playoffpool.repository.SeasonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminDivisionService {

    private final DivisionRepository divisionRepository;
    private final ParticipantRepository participantRepository;
    private final SeasonRepository seasonRepository;

    public AdminDivisionService(DivisionRepository divisionRepository,
                                ParticipantRepository participantRepository,
                                SeasonRepository seasonRepository) {
        this.divisionRepository = divisionRepository;
        this.participantRepository = participantRepository;
        this.seasonRepository = seasonRepository;
    }

    @Transactional(readOnly = true)
    public List<Division> getDivisionsForSeason(Integer seasonId) {
        return divisionRepository.findBySeasonIdOrderByNameAsc(seasonId);
    }

    @Transactional
    public Division createDivision(Integer seasonId, String name) {
        Season season = seasonRepository.findById(seasonId)
            .orElseThrow(() -> new RuntimeException("Season not found"));
        Division division = new Division();
        division.setName(name.trim());
        division.setSeason(season);
        return divisionRepository.save(division);
    }

    @Transactional
    public void addParticipantToDivision(Integer divisionId, Integer participantId) {
        Division division = divisionRepository.findById(divisionId)
            .orElseThrow(() -> new RuntimeException("Division not found"));
        Participant participant = participantRepository.findById(participantId)
            .orElseThrow(() -> new RuntimeException("Participant not found"));
        division.getParticipants().add(participant);
        divisionRepository.save(division);
    }

    @Transactional
    public void removeParticipantFromDivision(Integer divisionId, Integer participantId) {
        Division division = divisionRepository.findById(divisionId)
            .orElseThrow(() -> new RuntimeException("Division not found"));
        division.getParticipants().removeIf(p -> p.getId().equals(participantId));
        divisionRepository.save(division);
    }
}
