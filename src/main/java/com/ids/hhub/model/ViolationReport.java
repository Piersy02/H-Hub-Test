package com.ids.hhub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    // Mostra solo nome e ID del team
    @JsonIgnoreProperties({"members", "hackathon", "submission", "leader"})
    private Team reportedTeam;

    @ManyToOne
    @JoinColumn(name = "mentor_id")
    // Mostra solo nome e cognome ed email del mentore
    @JsonIgnoreProperties({"password", "id", "platformRole", "staffAssignments", "team"})
    private User mentor; // Chi ha fatto la segnalazione

    @ManyToOne
    @JoinColumn(name = "hackathon_id")
    @JsonIgnore
    private Hackathon hackathon;

    public ViolationReport(String description, Team reportedTeam, User mentor, Hackathon hackathon) {
        this.description = description;
        this.reportedTeam = reportedTeam;
        this.mentor = mentor;
        this.hackathon = hackathon;
    }
}