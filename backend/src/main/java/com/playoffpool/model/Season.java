package com.playoffpool.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seasons")
public class Season {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer year;

    @Column(length = 20)
    private String status = "active";

    @Column(name = "signup_content", columnDefinition = "TEXT")
    private String signupContent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Season() {
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSignupContent() { return signupContent; }
    public void setSignupContent(String signupContent) { this.signupContent = signupContent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
