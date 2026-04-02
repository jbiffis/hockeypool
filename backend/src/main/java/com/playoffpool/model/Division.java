package com.playoffpool.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "divisions")
public class Division {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    @JsonIgnore
    private Season season;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "participant_divisions",
        joinColumns = @JoinColumn(name = "division_id"),
        inverseJoinColumns = @JoinColumn(name = "participant_id")
    )
    @JsonIgnore
    private Set<Participant> participants = new HashSet<>();

    public Division() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Season getSeason() { return season; }
    public void setSeason(Season season) { this.season = season; }

    public Set<Participant> getParticipants() { return participants; }
    public void setParticipants(Set<Participant> participants) { this.participants = participants; }
}
