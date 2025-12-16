package com.ids.hhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class ViolationReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description; // Es. "Il team ha copiato codice da GitHub"

    @ManyToOne
    @JoinColumn(name = "team_id")
    @JsonIgnoreProperties("members")
    private Team reportedTeam;

    @ManyToOne
    @JoinColumn(name = "mentor_id")
    @JsonIgnoreProperties("staffAssignments")
    private User mentor; // Chi ha fatto la segnalazione

    @ManyToOne
    @JoinColumn(name = "hackathon_id")
    @JsonIgnoreProperties("teams")
    private Hackathon hackathon;

    public ViolationReport(String description, Team reportedTeam, User mentor, Hackathon hackathon) {
        this.description = description;
        this.reportedTeam = reportedTeam;
        this.mentor = mentor;
        this.hackathon = hackathon;
    }
}