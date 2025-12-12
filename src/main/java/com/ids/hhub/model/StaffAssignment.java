package com.ids.hhub.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "hackathon_id"})})
@Data
@NoArgsConstructor
public class StaffAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "hackathon_id")
    private Hackathon hackathon;

    @Enumerated(EnumType.STRING)
    private StaffRole role;

    public StaffAssignment(User user, Hackathon hackathon, StaffRole role) {
        this.user = user;
        this.hackathon = hackathon;
        this.role = role;
    }
}
