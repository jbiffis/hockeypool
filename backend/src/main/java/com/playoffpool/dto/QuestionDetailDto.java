package com.playoffpool.dto;

import java.util.List;

public class QuestionDetailDto {

    private Integer questionId;
    private String title;
    private String description;
    private String imageUrl;
    private String questionType;
    private Integer maxWager;
    private Integer maxSelections;
    private String correctAnswerText;
    private Integer roundId;
    private String roundName;
    private List<OptionDetail> options;

    public QuestionDetailDto() {}

    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public Integer getMaxWager() { return maxWager; }
    public void setMaxWager(Integer maxWager) { this.maxWager = maxWager; }

    public Integer getMaxSelections() { return maxSelections; }
    public void setMaxSelections(Integer maxSelections) { this.maxSelections = maxSelections; }

    public String getCorrectAnswerText() { return correctAnswerText; }
    public void setCorrectAnswerText(String correctAnswerText) { this.correctAnswerText = correctAnswerText; }

    public Integer getRoundId() { return roundId; }
    public void setRoundId(Integer roundId) { this.roundId = roundId; }

    public String getRoundName() { return roundName; }
    public void setRoundName(String roundName) { this.roundName = roundName; }

    public List<OptionDetail> getOptions() { return options; }
    public void setOptions(List<OptionDetail> options) { this.options = options; }

    public static class OptionDetail {
        private Integer optionId;
        private String optionText;
        private String subtext;
        private Integer points;
        private boolean isCorrect;
        private List<PickerInfo> pickers;

        public OptionDetail() {}

        public Integer getOptionId() { return optionId; }
        public void setOptionId(Integer optionId) { this.optionId = optionId; }

        public String getOptionText() { return optionText; }
        public void setOptionText(String optionText) { this.optionText = optionText; }

        public String getSubtext() { return subtext; }
        public void setSubtext(String subtext) { this.subtext = subtext; }

        public Integer getPoints() { return points; }
        public void setPoints(Integer points) { this.points = points; }

        public boolean isCorrect() { return isCorrect; }
        public void setCorrect(boolean correct) { isCorrect = correct; }

        public List<PickerInfo> getPickers() { return pickers; }
        public void setPickers(List<PickerInfo> pickers) { this.pickers = pickers; }
    }

    public static class PickerInfo {
        private Integer participantId;
        private String name;
        private String teamName;

        public PickerInfo() {}

        public Integer getParticipantId() { return participantId; }
        public void setParticipantId(Integer participantId) { this.participantId = participantId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
    }
}
