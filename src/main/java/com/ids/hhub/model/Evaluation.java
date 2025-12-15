package com.ids.hhub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
public class Evaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int score; // 0-10
    private String comment;

    @ManyToOne
    @JoinColumn(name = "submission_id")
    @JsonIgnore
    private Submission submission;

    @ManyToOne
    @JoinColumn(name = "judge_id")
    private User judge;

    // Costruttore...
}