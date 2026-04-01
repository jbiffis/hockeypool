package com.playoffpool.dto;

import com.playoffpool.model.Participant;

public class ParticipantDto {

    private Integer id;
    private Integer seasonId;
    private String email;
    private String name;
    private String teamName;
    private boolean returning;

    public ParticipantDto() {
    }

    public static ParticipantDto fromEntity(Participant p, boolean returning) {
        ParticipantDto dto = new ParticipantDto();
        dto.setId(p.getId());
        dto.setSeasonId(p.getSeason() != null ? p.getSeason().getId() : null);
        dto.setEmail(p.getEmail());
        dto.setName(p.getName());
        dto.setTeamName(p.getTeamName());
        dto.setReturning(returning);
        return dto;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSeasonId() { return seasonId; }
    public void setSeasonId(Integer seasonId) { this.seasonId = seasonId; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public boolean isReturning() {
        return returning;
    }

    public void setReturning(boolean returning) {
        this.returning = returning;
    }
}
