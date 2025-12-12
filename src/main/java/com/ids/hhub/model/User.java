package com.ids.hhub.model;

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

    // Un utente può appartenere a un solo team (vincolo del testo)
    // Nota: Se il vincolo è "un solo team alla volta in assoluto", usiamo questo:
    @OneToOne(mappedBy = "leader") // O ManyToOne se membro semplice
    private Team currentTeam;

    // Lista dei ruoli staff (es. Giudice nell'Hackathon A, Mentore nel B)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<StaffAssignment> staffAssignments;
}
