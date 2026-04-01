package com.playoffpool.dto;

import java.util.List;

public class SubmitPicksDto {

    private Integer participantId;
    private List<PickAnswerDto> answers;

    public SubmitPicksDto() {
    }

    public Integer getParticipantId() {
        return participantId;
    }

    public void setParticipantId(Integer participantId) {
        this.participantId = participantId;
    }

    public List<PickAnswerDto> getAnswers() {
        return answers;
    }

    public void setAnswers(List<PickAnswerDto> answers) {
        this.answers = answers;
    }
}
