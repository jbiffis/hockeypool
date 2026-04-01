package com.playoffpool.model;

import jakarta.persistence.*;

@Entity
@Table(name = "response_answers", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"response_id", "question_id", "selected_option_id"})
})
public class ResponseAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    private Response response;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private QuestionOption selectedOption;

    @Column(name = "free_form_value", columnDefinition = "TEXT")
    private String freeFormValue;

    public ResponseAnswer() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public QuestionOption getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(QuestionOption selectedOption) {
        this.selectedOption = selectedOption;
    }

    public String getFreeFormValue() {
        return freeFormValue;
    }

    public void setFreeFormValue(String freeFormValue) {
        this.freeFormValue = freeFormValue;
    }
}
