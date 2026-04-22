package com.playoffpool.service;

import com.playoffpool.dto.LeaderboardDto;
import com.playoffpool.dto.LeaderboardDto.LeaderboardEntry;
import com.playoffpool.dto.LeaderboardDto.RoundInfo;
import com.playoffpool.model.*;
import com.playoffpool.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class LeaderboardService {

    private final ParticipantRepository participantRepository;
    private final RoundRepository roundRepository;
    private final ParticipantScoreRepository participantScoreRepository;
    private final DivisionRepository divisionRepository;

    public LeaderboardService(ParticipantRepository participantRepository,
                              RoundRepository roundRepository,
                              ParticipantScoreRepository participantScoreRepository,
                              DivisionRepository divisionRepository) {
        this.participantRepository = participantRepository;
        this.roundRepository = roundRepository;
        this.participantScoreRepository = participantScoreRepository;
        this.divisionRepository = divisionRepository;
    }

    @Transactional(readOnly = true)
    public LeaderboardDto getLeaderboard(Integer seasonId, Integer divisionId) {
        LeaderboardDto dto = new LeaderboardDto();

        List<Round> allRounds = roundRepository.findBySeasonIdOrderByDisplayOrderAsc(seasonId);

        List<RoundInfo> roundInfos = new ArrayList<>();
        for (Round r : allRounds) {
            RoundInfo ri = new RoundInfo();
            ri.setId(r.getId());
            ri.setName(r.getName());
            ri.setDisplayOrder(r.getDisplayOrder());
            ri.setScored("scored".equals(r.getStatus()));
            roundInfos.add(ri);
        }
        dto.setRounds(roundInfos);

        List<Participant> participants;
        if (divisionId != null) {
            Division division = divisionRepository.findById(divisionId)
                .orElseThrow(() -> new RuntimeException("Division not found"));
            participants = new ArrayList<>(division.getParticipants());
        } else {
            participants = participantRepository.findBySeasonId(seasonId);
        }

        Map<Integer, List<ParticipantScore>> scoresByRound = new HashMap<>();
        for (Round r : allRounds) {
            scoresByRound.put(r.getId(), participantScoreRepository.findByRoundId(r.getId()));
        }

        List<LeaderboardEntry> entries = new ArrayList<>();
        for (Participant p : participants) {
            LeaderboardEntry entry = new LeaderboardEntry();
            entry.setParticipantId(p.getId());
            entry.setName(p.getName());
            entry.setTeamName(p.getTeamName());

            Map<Integer, Integer> roundScores = new HashMap<>();
            int total = 0;

            for (Round r : allRounds) {
                List<ParticipantScore> scores = scoresByRound.getOrDefault(r.getId(), Collections.emptyList());
                int roundTotal = scores.stream()
                        .filter(s -> s.getParticipant().getId().equals(p.getId()))
                        .mapToInt(ParticipantScore::getPointsEarned)
                        .sum();
                boolean hasScores = scores.stream()
                        .anyMatch(s -> s.getParticipant().getId().equals(p.getId()));
                if (hasScores) {
                    roundScores.put(r.getId(), roundTotal);
                    total += roundTotal;
                }
            }

            entry.setRoundScores(roundScores);
            entry.setOverallTotal(total);
            entries.add(entry);
        }

        dto.setEntries(entries);
        dto.setLastUpdatedAt(participantScoreRepository.findMaxUpdatedAtForSeason(seasonId));
        return dto;
    }
}
