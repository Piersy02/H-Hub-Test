package com.ids.hhub.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ids.hhub.model.enums.HackathonStatus;
import com.ids.hhub.model.state.HackathonState;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import com.ids.hhub.model.state.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hackathon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
    private String description;
    private String rules;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationDeadline;

    private int maxTeamSize;
    private double prizeAmount;

    @Enumerated(EnumType.STRING)
    private HackathonStatus status = HackathonStatus.REGISTRATION_OPEN;

    // Team iscritti a questo hackathon
    @OneToMany(mappedBy = "hackathon")
    @JsonIgnoreProperties("hackathon") // Alternativa: mostra il team ma non il campo 'hackathon' dentro il team
    private List<Team> teams;

    // Staff assegnato (Organizzatore, Giudici, Mentori)
    @OneToMany(mappedBy = "hackathon", cascade = CascadeType.ALL)
    private List<StaffAssignment> staff;

    @OneToOne
    @JoinColumn(name = "winner_team_id")
    @JsonIgnoreProperties({"hackathon", "members", "submission"}) // Mostra solo nome e ID del team vincente
    private Team winner;

    // Metodo che restituisce l'oggetto Stato corretto in base all'Enum salvato nel DB
    @JsonIgnore
    public HackathonState getCurrentStateObject() {
        switch (this.status) { // 'state' è l'Enum
            case REGISTRATION_OPEN:
                return new RegistrationOpenState();
            case ONGOING:
                return new OngoingState();
            case EVALUATION:
                return new EvaluationState();
            case FINISHED:
                return new FinishedState();
            default:
                throw new IllegalArgumentException("Stato sconosciuto: " + this.status);
        }
    }

    // Metodo delegato: L'Hackathon non fa il lavoro, lo passa allo State (pattern)
    public void registerTeam(Team team) {
        getCurrentStateObject().registerTeam(this, team);
    }

}
