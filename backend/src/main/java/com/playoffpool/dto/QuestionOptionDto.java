package com.playoffpool.dto;

import com.playoffpool.model.QuestionOption;

public class QuestionOptionDto {

    private Integer id;
    private Integer questionId;
    private String optionText;
    private Integer displayOrder;
    private Integer points;
    private String subtext;

    public QuestionOptionDto() {
    }

    public static QuestionOptionDto fromEntity(QuestionOption o) {
        QuestionOptionDto dto = new QuestionOptionDto();
        dto.setId(o.getId());
        dto.setQuestionId(o.getQuestion() != null ? o.getQuestion().getId() : null);
        dto.setOptionText(o.getOptionText());
        dto.setDisplayOrder(o.getDisplayOrder());
        dto.setPoints(o.getPoints());
        dto.setSubtext(o.getSubtext());
        return dto;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getSubtext() {
        return subtext;
    }

    public void setSubtext(String subtext) {
        this.subtext = subtext;
    }
}
