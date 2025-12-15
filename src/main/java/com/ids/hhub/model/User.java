package com.ids.hhub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ids.hhub.model.enums.PlatformRole;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;
    private String password; // Hashata
    private String name;
    private String surname;

    @Enumerated(EnumType.STRING)
    private PlatformRole platformRole = PlatformRole.USER;

    // Lista dei ruoli staff (es. Giudice nell'Hackathon A, Mentore nel B)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<StaffAssignment> staffAssignments;

    @ManyToOne // O @OneToOne se vuoi vincolo stretto, ma ManyToOne è più flessibile per storici futuri
    @JoinColumn(name = "team_id")
    @JsonIgnore // Importante: se stampo l'utente non voglio ristampare tutto il team
    @ToString.Exclude
    private Team team;
}
