package com.playoffpool.dto;

import com.playoffpool.model.Division;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DivisionDto {

    private Integer id;
    private String name;
    private Integer seasonId;
    private List<ParticipantSummary> participants;

    public DivisionDto() {}

    public static DivisionDto from(Division d) {
        DivisionDto dto = new DivisionDto();
        dto.setId(d.getId());
        dto.setName(d.getName());
        dto.setSeasonId(d.getSeason().getId());
        dto.setParticipants(d.getParticipants().stream()
            .map(p -> new ParticipantSummary(p.getId(), p.getName(), p.getTeamName()))
            .sorted(Comparator.comparing(ParticipantSummary::getTeamName, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList()));
        return dto;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getSeasonId() { return seasonId; }
    public void setSeasonId(Integer seasonId) { this.seasonId = seasonId; }

    public List<ParticipantSummary> getParticipants() { return participants; }
    public void setParticipants(List<ParticipantSummary> participants) { this.participants = participants; }

    public static class ParticipantSummary {
        private Integer id;
        private String name;
        private String teamName;

        public ParticipantSummary() {}

        public ParticipantSummary(Integer id, String name, String teamName) {
            this.id = id;
            this.name = name;
            this.teamName = teamName;
        }

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
    }
}
