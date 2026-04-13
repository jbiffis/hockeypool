package com.playoffpool.service;

import com.playoffpool.model.Division;
import com.playoffpool.model.Participant;
import com.playoffpool.model.Season;
import com.playoffpool.repository.DivisionRepository;
import com.playoffpool.repository.ParticipantRepository;
import com.playoffpool.repository.SeasonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDivisionServiceTest {

    @Mock private DivisionRepository divisionRepository;
    @Mock private ParticipantRepository participantRepository;
    @Mock private SeasonRepository seasonRepository;

    @InjectMocks
    private AdminDivisionService service;

    private Season season;
    private Division division;
    private Participant participant;

    @BeforeEach
    void setUp() {
        season = new Season();
        season.setId(1);
        season.setName("2025 Playoffs");

        division = new Division();
        division.setId(1);
        division.setName("East");
        division.setSeason(season);
        division.setParticipants(new HashSet<>());

        participant = new Participant();
        participant.setId(1);
        participant.setName("Test Player");
        participant.setEmail("test@example.com");
        participant.setSeason(season);
    }

    @Test
    void getDivisionsForSeason_returnsList() {
        Division d2 = new Division();
        d2.setId(2);
        d2.setName("West");
        d2.setSeason(season);

        when(divisionRepository.findBySeasonIdOrderByNameAsc(1)).thenReturn(List.of(division, d2));

        List<Division> result = service.getDivisionsForSeason(1);

        assertEquals(2, result.size());
        assertEquals("East", result.get(0).getName());
    }

    @Test
    void createDivision_createsWithTrimmedName() {
        when(seasonRepository.findById(1)).thenReturn(Optional.of(season));
        when(divisionRepository.save(any(Division.class))).thenAnswer(inv -> {
            Division d = inv.getArgument(0);
            d.setId(3);
            return d;
        });

        Division result = service.createDivision(1, "  West  ");

        assertEquals("West", result.getName());
        assertEquals(season, result.getSeason());
    }

    @Test
    void createDivision_nonExistingSeason_throwsException() {
        when(seasonRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.createDivision(999, "Test"));
    }

    @Test
    void addParticipantToDivision_addsSuccessfully() {
        when(divisionRepository.findById(1)).thenReturn(Optional.of(division));
        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(divisionRepository.save(any(Division.class))).thenAnswer(inv -> inv.getArgument(0));

        service.addParticipantToDivision(1, 1);

        assertTrue(division.getParticipants().contains(participant));
        verify(divisionRepository).save(division);
    }

    @Test
    void addParticipantToDivision_nonExistingDivision_throwsException() {
        when(divisionRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.addParticipantToDivision(999, 1));
    }

    @Test
    void addParticipantToDivision_nonExistingParticipant_throwsException() {
        when(divisionRepository.findById(1)).thenReturn(Optional.of(division));
        when(participantRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.addParticipantToDivision(1, 999));
    }

    @Test
    void removeParticipantFromDivision_removesSuccessfully() {
        division.getParticipants().add(participant);
        when(divisionRepository.findById(1)).thenReturn(Optional.of(division));
        when(divisionRepository.save(any(Division.class))).thenAnswer(inv -> inv.getArgument(0));

        service.removeParticipantFromDivision(1, 1);

        assertFalse(division.getParticipants().contains(participant));
        verify(divisionRepository).save(division);
    }

    @Test
    void removeParticipantFromDivision_nonExistingDivision_throwsException() {
        when(divisionRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.removeParticipantFromDivision(999, 1));
    }

    @Test
    void removeParticipantFromDivision_participantNotInDivision_noError() {
        when(divisionRepository.findById(1)).thenReturn(Optional.of(division));
        when(divisionRepository.save(any(Division.class))).thenAnswer(inv -> inv.getArgument(0));

        // Should not throw even if participant is not in division
        service.removeParticipantFromDivision(1, 999);

        verify(divisionRepository).save(division);
    }
}
