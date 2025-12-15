package com.ids.hhub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String projectUrl; // Link a GitHub/Drive
    private String description;

    private LocalDateTime submissionDate;

    @OneToOne
    @JoinColumn(name = "team_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Team team;

    public Submission(String projectUrl, String description, Team team) {
        this.projectUrl = projectUrl;
        this.description = description;
        this.team = team;
        this.submissionDate = LocalDateTime.now();
    }
}