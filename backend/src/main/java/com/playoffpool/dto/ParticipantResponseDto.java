package com.playoffpool.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ParticipantResponseDto {

    private Integer participantId;
    private String participantName;
    private String teamName;
    private String email;
    private Integer roundId;
    private String roundName;
    private Integer roundDisplayOrder;
    private LocalDateTime submittedAt;
    private Integer roundPointsTotal;
    private List<AnswerDto> answers;

    public ParticipantResponseDto() {
    }

    public Integer getParticipantId() {
        return participantId;
    }

    public void setParticipantId(Integer participantId) {
        this.participantId = participantId;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getRoundId() { return roundId; }
    public void setRoundId(Integer roundId) { this.roundId = roundId; }

    public String getRoundName() { return roundName; }
    public void setRoundName(String roundName) { this.roundName = roundName; }

    public Integer getRoundDisplayOrder() { return roundDisplayOrder; }
    public void setRoundDisplayOrder(Integer roundDisplayOrder) { this.roundDisplayOrder = roundDisplayOrder; }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Integer getRoundPointsTotal() { return roundPointsTotal; }
    public void setRoundPointsTotal(Integer roundPointsTotal) { this.roundPointsTotal = roundPointsTotal; }

    public List<AnswerDto> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerDto> answers) {
        this.answers = answers;
    }
}
