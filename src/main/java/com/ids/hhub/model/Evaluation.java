package com.ids.hhub.model;

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
    private Submission submission;

    @ManyToOne
    private User judge; // Il giudice che ha votato
}