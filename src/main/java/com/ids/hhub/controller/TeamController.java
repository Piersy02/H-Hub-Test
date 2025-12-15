package com.ids.hhub.controller;

import com.ids.hhub.dto.CreateTeamDto;
import com.ids.hhub.dto.RegisterTeamByNameDto;
import com.ids.hhub.dto.inviteUserDto;
import com.ids.hhub.model.Team;
import com.ids.hhub.model.TeamInvitation;
import com.ids.hhub.service.TeamService;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    // 1. CREA TEAM (POST /api/teams)
    @PostMapping
    public ResponseEntity<Team> createTeam(@RequestBody CreateTeamDto dto, Authentication auth) {
        return ResponseEntity.ok(teamService.createTeam(dto.getName(), auth.getName()));
    }

    /// POST /api/teams/register
    @PostMapping("/register")
    public ResponseEntity<String> registerTeamByName(
            @RequestBody RegisterTeamByNameDto dto,
            Authentication auth
    ) {
        teamService.registerTeamByName(dto.getTeamName(), dto.getHackathonName(), auth.getName());

        return ResponseEntity.ok("Team " + dto.getTeamName() +
                " iscritto con successo a " + dto.getHackathonName() + "!");
    }

    // 1. INVIA INVITO (Leader)
    @PostMapping("/{teamId}/invite")
    public ResponseEntity<String> inviteUser(
            @PathVariable Long teamId,
            @RequestBody inviteUserDto dto,
            Authentication auth
    ) {
        teamService.sendInvitation(teamId, dto.getUserEmail(), auth.getName());
        return ResponseEntity.ok("Invito inviato a " + dto.getUserEmail());
    }

    // 2. VEDI I MIEI INVITI (Utente)
    @GetMapping("/invitations")
    public ResponseEntity<List<TeamInvitation>> getMyInvitations(Authentication auth) {
        return ResponseEntity.ok(teamService.getMyPendingInvitations(auth.getName()));
    }

    // 3. ACCETTA INVITO (Utente)
    @PostMapping("/invitations/{invitationId}/accept")
    public ResponseEntity<String> acceptInvitation(
            @PathVariable Long invitationId,
            Authentication auth
    ) {
        teamService.acceptInvitation(invitationId, auth.getName());
        return ResponseEntity.ok("Invito accettato! Benvenuto nel team.");
    }

    //Se Mario guarda la lista membri del team, vedrà Luigi solo se ha accettato l'invito.
    @PostMapping("/invitations/{invitationId}/reject")
    public ResponseEntity<String> rejectInvitation(
            @PathVariable Long invitationId,
            Authentication auth
    ) {
        teamService.rejectInvitation(invitationId, auth.getName());
        return ResponseEntity.ok("Invito rifiutato correttamente.");
    }

}