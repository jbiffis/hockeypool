package com.playoffpool.dto;

import com.playoffpool.model.Round;
import com.playoffpool.repository.RoundRepository;

import java.time.LocalDateTime;

public class RoundDto {

    private Integer id;
    private Integer seasonId;
    private String name;
    private String description;
    private LocalDateTime deadline;
    private String status;
    private Integer displayOrder;
    private Integer displayWithRoundId;
    private LocalDateTime createdAt;

    public RoundDto() {
    }

    public static RoundDto fromEntity(Round round) {
        RoundDto dto = new RoundDto();
        dto.setId(round.getId());
        dto.setSeasonId(round.getSeason() != null ? round.getSeason().getId() : null);
        dto.setName(round.getName());
        dto.setDescription(round.getDescription());
        dto.setDeadline(round.getDeadline());
        dto.setStatus(round.getStatus());
        dto.setDisplayOrder(round.getDisplayOrder());
        if (round.getDisplayWithRound() != null) {
            dto.setDisplayWithRoundId(round.getDisplayWithRound().getId());
        }
        dto.setCreatedAt(round.getCreatedAt());
        return dto;
    }

    public void applyToEntity(Round round, RoundRepository roundRepo) {
        round.setName(this.name);
        round.setDescription(this.description);
        round.setDeadline(this.deadline);
        round.setStatus(this.status);
        round.setDisplayOrder(this.displayOrder);
        if (this.displayWithRoundId != null) {
            Round displayWithRound = roundRepo.findById(this.displayWithRoundId).orElse(null);
            round.setDisplayWithRound(displayWithRound);
        } else {
            round.setDisplayWithRound(null);
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSeasonId() { return seasonId; }
    public void setSeasonId(Integer seasonId) { this.seasonId = seasonId; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Integer getDisplayWithRoundId() {
        return displayWithRoundId;
    }

    public void setDisplayWithRoundId(Integer displayWithRoundId) {
        this.displayWithRoundId = displayWithRoundId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
