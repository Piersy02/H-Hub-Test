package com.ids.hhub.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "hackathon_id")
    private Hackathon hackathon;

    @OneToOne
    private User leader; // Chi ha creato il team

    @OneToMany
    private List<User> members; // Lista membri inclusi leader

    @OneToOne(mappedBy = "team", cascade = CascadeType.ALL)
    private Submission submission;
}