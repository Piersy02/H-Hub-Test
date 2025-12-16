package com.ids.hhub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ids.hhub.model.enums.StaffRole;
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
    //fa vedere solo nome e cognome
    @JsonIgnoreProperties({"password", "email", "id", "platformRole", "staffAssignments", "team", "currentTeam", "createdAt"})
    private User user;

    @ManyToOne
    @JoinColumn(name = "hackathon_id")
    @JsonIgnore
    @ToString.Exclude
    private Hackathon hackathon;

    @Enumerated(EnumType.STRING)
    private StaffRole role;

    public StaffAssignment(User user, Hackathon hackathon, StaffRole role) {
        this.user = user;
        this.hackathon = hackathon;
        this.role = role;
    }
}
