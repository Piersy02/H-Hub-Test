package com.ids.hhub.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.ArrayList; // <--- Importante
import java.util.List;      // <--- Importante

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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submissionDate;

    @OneToOne
    @JoinColumn(name = "team_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Team team;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnore // Evita che quando stampi la submission ti stampi infinite valutazioni
    private List<Evaluation> evaluations = new ArrayList<>(); // Inizializzala per evitare NullPointerException

    public Submission(String projectUrl, String description, Team team) {
        this.projectUrl = projectUrl;
        this.description = description;
        this.team = team;
        this.submissionDate = LocalDateTime.now();
    }
}