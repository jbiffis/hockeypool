package com.playoffpool.dto;

import com.playoffpool.model.Question;

import java.time.LocalDateTime;

public class QuestionDto {

    private Integer id;
    private Integer roundId;
    private String title;
    private String description;
    private String imageUrl;
    private String questionType;
    private Boolean isMandatory;
    private Integer displayOrder;
    private Integer maxWager;
    private Integer maxSelections;
    private Integer points;
    private Integer parentQuestionId;
    private LocalDateTime createdAt;

    public QuestionDto() {
    }

    public static QuestionDto fromEntity(Question q) {
        QuestionDto dto = new QuestionDto();
        dto.setId(q.getId());
        dto.setRoundId(q.getRound() != null ? q.getRound().getId() : null);
        dto.setTitle(q.getTitle());
        dto.setDescription(q.getDescription());
        dto.setImageUrl(q.getImageUrl());
        dto.setQuestionType(q.getQuestionType());
        dto.setIsMandatory(q.getIsMandatory());
        dto.setDisplayOrder(q.getDisplayOrder());
        dto.setMaxWager(q.getMaxWager());
        dto.setMaxSelections(q.getMaxSelections());
        dto.setPoints(q.getPoints());
        dto.setParentQuestionId(q.getParentQuestion() != null ? q.getParentQuestion().getId() : null);
        dto.setCreatedAt(q.getCreatedAt());
        return dto;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRoundId() {
        return roundId;
    }

    public void setRoundId(Integer roundId) {
        this.roundId = roundId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public Boolean getIsMandatory() {
        return isMandatory;
    }

    public void setIsMandatory(Boolean isMandatory) {
        this.isMandatory = isMandatory;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Integer getMaxWager() {
        return maxWager;
    }

    public void setMaxWager(Integer maxWager) {
        this.maxWager = maxWager;
    }

    public Integer getMaxSelections() {
        return maxSelections;
    }

    public void setMaxSelections(Integer maxSelections) {
        this.maxSelections = maxSelections;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getParentQuestionId() {
        return parentQuestionId;
    }

    public void setParentQuestionId(Integer parentQuestionId) {
        this.parentQuestionId = parentQuestionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
