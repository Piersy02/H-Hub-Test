package com.ids.hhub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // Un team appartiene a un solo Hackathon
    @ManyToOne
    @JoinColumn(name = "hackathon_id", nullable = true)
    @JsonIgnore // Evita loop JSON
    @ToString.Exclude
    private Hackathon hackathon;

    // Il Leader è anche un membro, ma lo salviamo a parte per comodità
    @OneToOne
    @JoinColumn(name = "leader_id")
    private User leader;

    // Lista dei membri (incluso il leader)
    @OneToMany(mappedBy = "team", fetch = FetchType.EAGER)
    private List<User> members = new ArrayList<>();

    @OneToOne(mappedBy = "team", cascade = CascadeType.ALL)
    private Submission submission;

    // Costruttore per team "vuoto"
    public Team(String name, User leader) {
        this.name = name;
        this.leader = leader;
    }
}