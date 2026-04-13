package com.playoffpool.service;

import com.playoffpool.dto.LeaderboardDto;
import com.playoffpool.model.*;
import com.playoffpool.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock private ParticipantRepository participantRepository;
    @Mock private RoundRepository roundRepository;
    @Mock private ParticipantScoreRepository participantScoreRepository;
    @Mock private DivisionRepository divisionRepository;

    @InjectMocks
    private LeaderboardService service;

    private Season season;
    private Round round1, round2;
    private Participant participant1, participant2;

    @BeforeEach
    void setUp() {
        season = new Season();
        season.setId(1);

        round1 = new Round();
        round1.setId(1);
        round1.setName("Round 1");
        round1.setDisplayOrder(1);
        round1.setStatus("scored");
        round1.setSeason(season);

        round2 = new Round();
        round2.setId(2);
        round2.setName("Round 2");
        round2.setDisplayOrder(2);
        round2.setStatus("open");
        round2.setSeason(season);

        participant1 = new Participant();
        participant1.setId(1);
        participant1.setName("Player 1");
        participant1.setTeamName("Team Alpha");
        participant1.setSeason(season);

        participant2 = new Participant();
        participant2.setId(2);
        participant2.setName("Player 2");
        participant2.setTeamName("Team Beta");
        participant2.setSeason(season);
    }

    @Test
    void getLeaderboard_returnsRoundInfos() {
        when(roundRepository.findBySeasonIdOrderByDisplayOrderAsc(1)).thenReturn(List.of(round1, round2));
        when(participantRepository.findBySeasonId(1)).thenReturn(Collections.emptyList());
        when(participantScoreRepository.findByRoundId(1)).thenReturn(Collections.emptyList());
        when(participantScoreRepository.findByRoundId(2)).thenReturn(Collections.emptyList());

        LeaderboardDto result = service.getLeaderboard(1, null);

        assertEquals(2, result.getRounds().size());
        assertEquals("Round 1", result.getRounds().get(0).getName());
        assertTrue(result.getRounds().get(0).isScored());
        assertFalse(result.getRounds().get(1).isScored());
    }

    @Test
    void getLeaderboard_computesScoresCorrectly() {
        when(roundRepository.findBySeasonIdOrderByDisplayOrderAsc(1)).thenReturn(List.of(round1));
        when(participantRepository.findBySeasonId(1)).thenReturn(List.of(participant1, participant2));

        Question q1 = new Question();
        q1.setId(1);

        ParticipantScore score1 = new ParticipantScore();
        score1.setParticipant(participant1);
        score1.setQuestion(q1);
        score1.setRound(round1);
        score1.setPointsEarned(15);

        ParticipantScore score2 = new ParticipantScore();
        score2.setParticipant(participant2);
        score2.setQuestion(q1);
        score2.setRound(round1);
        score2.setPointsEarned(10);

        when(participantScoreRepository.findByRoundId(1)).thenReturn(List.of(score1, score2));

        LeaderboardDto result = service.getLeaderboard(1, null);

        assertEquals(2, result.getEntries().size());

        LeaderboardDto.LeaderboardEntry entry1 = result.getEntries().stream()
                .filter(e -> e.getParticipantId().equals(1)).findFirst().orElseThrow();
        assertEquals(15, entry1.getOverallTotal());
        assertEquals(15, entry1.getRoundScores().get(1));

        LeaderboardDto.LeaderboardEntry entry2 = result.getEntries().stream()
                .filter(e -> e.getParticipantId().equals(2)).findFirst().orElseThrow();
        assertEquals(10, entry2.getOverallTotal());
    }

    @Test
    void getLeaderboard_multipleRounds_aggregatesTotal() {
        when(roundRepository.findBySeasonIdOrderByDisplayOrderAsc(1)).thenReturn(List.of(round1, round2));
        when(participantRepository.findBySeasonId(1)).thenReturn(List.of(participant1));

        Question q1 = new Question();
        q1.setId(1);
        Question q2 = new Question();
        q2.setId(2);

        ParticipantScore s1 = new ParticipantScore();
        s1.setParticipant(participant1);
        s1.setQuestion(q1);
        s1.setRound(round1);
        s1.setPointsEarned(20);

        ParticipantScore s2 = new ParticipantScore();
        s2.setParticipant(participant1);
        s2.setQuestion(q2);
        s2.setRound(round2);
        s2.setPointsEarned(30);

        when(participantScoreRepository.findByRoundId(1)).thenReturn(List.of(s1));
        when(participantScoreRepository.findByRoundId(2)).thenReturn(List.of(s2));

        LeaderboardDto result = service.getLeaderboard(1, null);

        LeaderboardDto.LeaderboardEntry entry = result.getEntries().get(0);
        assertEquals(50, entry.getOverallTotal());
        assertEquals(20, entry.getRoundScores().get(1));
        assertEquals(30, entry.getRoundScores().get(2));
    }

    @Test
    void getLeaderboard_withDivisionFilter_filtersParticipants() {
        Division division = new Division();
        division.setId(10);
        division.setName("East");
        division.setSeason(season);
        division.setParticipants(Set.of(participant1));

        when(roundRepository.findBySeasonIdOrderByDisplayOrderAsc(1)).thenReturn(List.of(round1));
        when(divisionRepository.findById(10)).thenReturn(Optional.of(division));
        when(participantScoreRepository.findByRoundId(1)).thenReturn(Collections.emptyList());

        LeaderboardDto result = service.getLeaderboard(1, 10);

        assertEquals(1, result.getEntries().size());
        assertEquals("Player 1", result.getEntries().get(0).getName());
    }

    @Test
    void getLeaderboard_noScores_returnsZeroTotals() {
        when(roundRepository.findBySeasonIdOrderByDisplayOrderAsc(1)).thenReturn(List.of(round1));
        when(participantRepository.findBySeasonId(1)).thenReturn(List.of(participant1));
        when(participantScoreRepository.findByRoundId(1)).thenReturn(Collections.emptyList());

        LeaderboardDto result = service.getLeaderboard(1, null);

        LeaderboardDto.LeaderboardEntry entry = result.getEntries().get(0);
        assertEquals(0, entry.getOverallTotal());
        assertTrue(entry.getRoundScores().isEmpty());
    }

    @Test
    void getLeaderboard_nonExistingDivision_throwsException() {
        when(roundRepository.findBySeasonIdOrderByDisplayOrderAsc(1)).thenReturn(List.of(round1));
        when(divisionRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getLeaderboard(1, 999));
    }
}
