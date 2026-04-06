package com.playoffpool.service;

import com.playoffpool.dto.SeasonDto;
import com.playoffpool.model.Round;
import com.playoffpool.model.Season;
import com.playoffpool.model.Participant;
import com.playoffpool.repository.ParticipantRepository;
import com.playoffpool.repository.SeasonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AdminSeasonService {

    private final SeasonRepository seasonRepository;
    private final AdminRoundService adminRoundService;
    private final ParticipantRepository participantRepository;

    public AdminSeasonService(SeasonRepository seasonRepository,
                              AdminRoundService adminRoundService,
                              ParticipantRepository participantRepository) {
        this.seasonRepository = seasonRepository;
        this.adminRoundService = adminRoundService;
        this.participantRepository = participantRepository;
    }

    public List<Season> getAllSeasons() {
        return seasonRepository.findAllByOrderByYearDesc();
    }

    public Season getSeason(Integer id) {
        return seasonRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Season not found with id: " + id));
    }

    @Transactional
    public Season createSeason(SeasonDto dto) {
        Season season = new Season();
        season.setName(dto.getName());
        season.setYear(dto.getYear());
        season.setStatus(dto.getStatus() != null ? dto.getStatus() : "archived");
        season.setSignupContent(dto.getSignupContent());
        season.setCreatedAt(LocalDateTime.now());
        if ("active".equals(season.getStatus())) {
            archiveAllSeasons();
        }
        return seasonRepository.save(season);
    }

    @Transactional
    public Season updateSeason(Integer id, SeasonDto dto) {
        Season season = getSeason(id);
        season.setName(dto.getName());
        season.setYear(dto.getYear());
        if (dto.getStatus() != null && "active".equals(dto.getStatus()) && !"active".equals(season.getStatus())) {
            archiveAllSeasons();
        }
        season.setStatus(dto.getStatus());
        season.setSignupContent(dto.getSignupContent());
        return seasonRepository.save(season);
    }

    @Transactional
    public void deleteSeason(Integer id) {
        // Delete all rounds (which cascades to questions, options, responses, scores)
        List<Round> rounds = adminRoundService.getRoundsBySeason(id);
        for (Round r : rounds) {
            adminRoundService.deleteRound(r.getId());
        }
        // Delete all participants for this season
        List<Participant> participants = participantRepository.findBySeasonId(id);
        participantRepository.deleteAll(participants);

        seasonRepository.deleteById(id);
    }

    private void archiveAllSeasons() {
        for (Season s : seasonRepository.findAll()) {
            if ("active".equals(s.getStatus())) {
                s.setStatus("archived");
                seasonRepository.save(s);
            }
        }
    }
}
