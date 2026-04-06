package com.playoffpool.dto;

import com.playoffpool.model.Season;
import java.time.LocalDateTime;

public class SeasonDto {

    private Integer id;
    private String name;
    private Integer year;
    private String status;
    private String signupContent;
    private LocalDateTime createdAt;

    public SeasonDto() {}

    public static SeasonDto fromEntity(Season s) {
        SeasonDto dto = new SeasonDto();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setYear(s.getYear());
        dto.setStatus(s.getStatus());
        dto.setSignupContent(s.getSignupContent());
        dto.setCreatedAt(s.getCreatedAt());
        return dto;
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
