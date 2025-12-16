package com.ids.hhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class SupportRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String problemDescription; // Es. "Non riusciamo a deployare su AWS"

    private String meetingLink; // Qui salveremo il link generato dal Calendar (Strategy)

    private boolean resolved = false;

    @ManyToOne
    @JoinColumn(name = "team_id")
    @JsonIgnoreProperties({"members", "hackathon", "submission"})
    private Team team;

    @ManyToOne
    @JoinColumn(name = "hackathon_id")
    @JsonIgnoreProperties({"teams", "staff", "currentStateObject"})
    private Hackathon hackathon;

    public SupportRequest(String problemDescription, Team team, Hackathon hackathon) {
        this.problemDescription = problemDescription;
        this.team = team;
        this.hackathon = hackathon;
    }
}