package com.playoffpool.dto;

import java.util.List;

public class PickAnswerDto {

    private Integer questionId;
    private Integer selectedOptionId;
    private List<Integer> selectedOptionIds;
    private String freeFormValue;

    public PickAnswerDto() {
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public Integer getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(Integer selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }

    public List<Integer> getSelectedOptionIds() {
        return selectedOptionIds;
    }

    public void setSelectedOptionIds(List<Integer> selectedOptionIds) {
        this.selectedOptionIds = selectedOptionIds;
    }

    public String getFreeFormValue() {
        return freeFormValue;
    }

    public void setFreeFormValue(String freeFormValue) {
        this.freeFormValue = freeFormValue;
    }
}
