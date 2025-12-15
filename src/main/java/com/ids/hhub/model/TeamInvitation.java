package com.ids.hhub.model;

import com.ids.hhub.model.enums.InvitationStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class TeamInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team; // Chi invia

    @ManyToOne
    @JoinColumn(name = "invitee_id", nullable = false)
    private User invitee; // Chi riceve

    @Enumerated(EnumType.STRING)
    private InvitationStatus status = InvitationStatus.PENDING;

    public TeamInvitation(Team team, User invitee) {
        this.team = team;
        this.invitee = invitee;
        this.status = InvitationStatus.PENDING;
    }
}