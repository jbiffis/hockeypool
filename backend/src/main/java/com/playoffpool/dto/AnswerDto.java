package com.playoffpool.dto;

public class AnswerDto {

    private Integer questionId;
    private String questionTitle;
    private Integer selectedOptionId;
    private String selectedOptionText;
    private Integer optionPointValue;
    private String freeFormValue;
    private Integer pointsEarned;
    private String correctAnswerText;

    public AnswerDto() {
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public String getQuestionTitle() {
        return questionTitle;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    public Integer getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(Integer selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }

    public String getSelectedOptionText() {
        return selectedOptionText;
    }

    public void setSelectedOptionText(String selectedOptionText) {
        this.selectedOptionText = selectedOptionText;
    }

    public String getFreeFormValue() {
        return freeFormValue;
    }

    public Integer getOptionPointValue() { return optionPointValue; }
    public void setOptionPointValue(Integer optionPointValue) { this.optionPointValue = optionPointValue; }

    public void setFreeFormValue(String freeFormValue) {
        this.freeFormValue = freeFormValue;
    }

    public Integer getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(Integer pointsEarned) { this.pointsEarned = pointsEarned; }

    public String getCorrectAnswerText() { return correctAnswerText; }
    public void setCorrectAnswerText(String correctAnswerText) { this.correctAnswerText = correctAnswerText; }
}
