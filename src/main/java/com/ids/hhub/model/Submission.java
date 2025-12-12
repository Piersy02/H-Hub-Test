package com.ids.hhub.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String projectUrl; // Link GitHub/Drive
    private String description;
    private LocalDateTime submissionDate;

    @OneToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToMany(mappedBy = "submission")
    private List<Evaluation> evaluations;
}
