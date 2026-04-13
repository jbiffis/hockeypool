package com.playoffpool.service;

import com.playoffpool.dto.SeasonDto;
import com.playoffpool.model.Participant;
import com.playoffpool.model.Round;
import com.playoffpool.model.Season;
import com.playoffpool.repository.ParticipantRepository;
import com.playoffpool.repository.SeasonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminSeasonServiceTest {

    @Mock
    private SeasonRepository seasonRepository;

    @Mock
    private AdminRoundService adminRoundService;

    @Mock
    private ParticipantRepository participantRepository;

    @InjectMocks
    private AdminSeasonService service;

    private Season season;

    @BeforeEach
    void setUp() {
        season = new Season();
        season.setId(1);
        season.setName("2025 Playoffs");
        season.setYear(2025);
        season.setStatus("active");
        season.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getAllSeasons_returnsOrderedList() {
        Season s2 = new Season();
        s2.setId(2);
        s2.setName("2024 Playoffs");
        s2.setYear(2024);
        when(seasonRepository.findAllByOrderByYearDesc()).thenReturn(List.of(season, s2));

        List<Season> result = service.getAllSeasons();

        assertEquals(2, result.size());
        assertEquals(2025, result.get(0).getYear());
        assertEquals(2024, result.get(1).getYear());
    }

    @Test
    void getSeason_existingId_returnsSeason() {
        when(seasonRepository.findById(1)).thenReturn(Optional.of(season));

        Season result = service.getSeason(1);

        assertEquals("2025 Playoffs", result.getName());
    }

    @Test
    void getSeason_nonExistingId_throwsException() {
        when(seasonRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getSeason(999));
    }

    @Test
    void createSeason_withActiveStatus_archivesExisting() {
        Season existing = new Season();
        existing.setId(2);
        existing.setStatus("active");
        when(seasonRepository.findAll()).thenReturn(List.of(existing));
        when(seasonRepository.save(any(Season.class))).thenAnswer(inv -> inv.getArgument(0));

        SeasonDto dto = new SeasonDto();
        dto.setName("2025 Playoffs");
        dto.setYear(2025);
        dto.setStatus("active");

        Season result = service.createSeason(dto);

        assertEquals("active", result.getStatus());
        assertEquals("archived", existing.getStatus());
        verify(seasonRepository, atLeastOnce()).save(existing);
    }

    @Test
    void createSeason_withArchivedStatus_doesNotArchiveExisting() {
        when(seasonRepository.save(any(Season.class))).thenAnswer(inv -> inv.getArgument(0));

        SeasonDto dto = new SeasonDto();
        dto.setName("2024 Playoffs");
        dto.setYear(2024);
        dto.setStatus("archived");

        Season result = service.createSeason(dto);

        assertEquals("archived", result.getStatus());
        verify(seasonRepository, never()).findAll();
    }

    @Test
    void createSeason_nullStatus_defaultsToArchived() {
        when(seasonRepository.save(any(Season.class))).thenAnswer(inv -> inv.getArgument(0));

        SeasonDto dto = new SeasonDto();
        dto.setName("2024 Playoffs");
        dto.setYear(2024);
        dto.setStatus(null);

        Season result = service.createSeason(dto);

        assertEquals("archived", result.getStatus());
    }

    @Test
    void createSeason_setsCreatedAt() {
        when(seasonRepository.save(any(Season.class))).thenAnswer(inv -> inv.getArgument(0));

        SeasonDto dto = new SeasonDto();
        dto.setName("Test");
        dto.setYear(2025);

        Season result = service.createSeason(dto);

        assertNotNull(result.getCreatedAt());
    }

    @Test
    void updateSeason_changesFields() {
        when(seasonRepository.findById(1)).thenReturn(Optional.of(season));
        when(seasonRepository.save(any(Season.class))).thenAnswer(inv -> inv.getArgument(0));

        SeasonDto dto = new SeasonDto();
        dto.setName("Updated Name");
        dto.setYear(2026);
        dto.setStatus("archived");

        Season result = service.updateSeason(1, dto);

        assertEquals("Updated Name", result.getName());
        assertEquals(2026, result.getYear());
        assertEquals("archived", result.getStatus());
    }

    @Test
    void updateSeason_toActive_archivesOthers() {
        season.setStatus("archived");
        Season other = new Season();
        other.setId(2);
        other.setStatus("active");

        when(seasonRepository.findById(1)).thenReturn(Optional.of(season));
        when(seasonRepository.findAll()).thenReturn(List.of(season, other));
        when(seasonRepository.save(any(Season.class))).thenAnswer(inv -> inv.getArgument(0));

        SeasonDto dto = new SeasonDto();
        dto.setName("2025 Playoffs");
        dto.setYear(2025);
        dto.setStatus("active");

        service.updateSeason(1, dto);

        assertEquals("archived", other.getStatus());
    }

    @Test
    void deleteSeason_cascadesToRoundsAndParticipants() {
        Round r1 = new Round();
        r1.setId(10);
        Round r2 = new Round();
        r2.setId(11);
        when(adminRoundService.getRoundsBySeason(1)).thenReturn(List.of(r1, r2));

        Participant p1 = new Participant();
        p1.setId(100);
        when(participantRepository.findBySeasonId(1)).thenReturn(List.of(p1));

        service.deleteSeason(1);

        verify(adminRoundService).deleteRound(10);
        verify(adminRoundService).deleteRound(11);
        verify(participantRepository).deleteAll(List.of(p1));
        verify(seasonRepository).deleteById(1);
    }
}
