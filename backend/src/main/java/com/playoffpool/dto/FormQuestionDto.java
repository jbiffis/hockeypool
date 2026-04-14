package com.playoffpool.dto;

import com.playoffpool.model.Question;
import com.playoffpool.model.QuestionOption;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FormQuestionDto {

    private Integer id;
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
    private Integer roundId;
    private String roundName;
    private List<QuestionOptionDto> options;

    public FormQuestionDto() {
    }

    public static FormQuestionDto fromEntity(Question q, List<QuestionOption> options) {
        FormQuestionDto dto = new FormQuestionDto();
        dto.setId(q.getId());
        dto.setTitle(q.getTitle());
        dto.setDescription(q.getDescription());
        dto.setImageUrl(q.getImageUrl());
        dto.setQuestionType(q.getQuestionType());
        dto.setIsMandatory(q.getIsMandatory());
        dto.setDisplayOrder(q.getDisplayOrder());
        dto.setMaxWager(q.getMaxWager());
        dto.setMaxSelections(q.getMaxSelections());
        dto.setPoints(q.getPoints());
        if (q.getParentQuestion() != null) {
            dto.setParentQuestionId(q.getParentQuestion().getId());
        }
        if (q.getRound() != null) {
            dto.setRoundId(q.getRound().getId());
            dto.setRoundName(q.getRound().getName());
        }
        if (options != null) {
            dto.setOptions(options.stream()
                    .map(QuestionOptionDto::fromEntity)
                    .collect(Collectors.toList()));
        } else {
            dto.setOptions(Collections.emptyList());
        }
        return dto;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Integer getRoundId() {
        return roundId;
    }

    public void setRoundId(Integer roundId) {
        this.roundId = roundId;
    }

    public String getRoundName() {
        return roundName;
    }

    public void setRoundName(String roundName) {
        this.roundName = roundName;
    }

    public List<QuestionOptionDto> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOptionDto> options) {
        this.options = options;
    }
}
