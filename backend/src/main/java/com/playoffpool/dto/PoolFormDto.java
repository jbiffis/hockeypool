package com.playoffpool.dto;

import java.util.List;

public class PoolFormDto {

    private RoundDto round;
    private List<FormQuestionDto> questions;
    private String deadline;
    private boolean alreadySubmitted;

    public PoolFormDto() {
    }

    public RoundDto getRound() {
        return round;
    }

    public void setRound(RoundDto round) {
        this.round = round;
    }

    public List<FormQuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<FormQuestionDto> questions) {
        this.questions = questions;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public boolean isAlreadySubmitted() {
        return alreadySubmitted;
    }

    public void setAlreadySubmitted(boolean alreadySubmitted) {
        this.alreadySubmitted = alreadySubmitted;
    }
}
