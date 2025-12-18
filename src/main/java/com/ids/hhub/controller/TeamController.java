package com.ids.hhub.controller;

import com.ids.hhub.dto.CreateTeamDto;
import com.ids.hhub.dto.RegisterTeamByNameDto;
import com.ids.hhub.dto.inviteUserDto; // Nota: idealmente rinomina la classe in InviteUserDto (PascalCase)
import com.ids.hhub.model.Team;
import com.ids.hhub.model.TeamInvitation;
import com.ids.hhub.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@Tag(name = "Team Management", description = "API per la creazione dei team, iscrizione agli hackathon e gestione inviti")
public class TeamController {

    @Autowired
    private TeamService teamService;

    // =================================================================================
    // SEZIONE 1: GESTIONE TEAM (Creazione e Iscrizione)
    // =================================================================================

    @PostMapping
    @Operation(summary = "Crea un nuovo Team", description = "Qualsiasi utente registrato può creare un team. Chi lo crea diventa automaticamente il Leader.")
    public ResponseEntity<Team> createTeam(
            @RequestBody CreateTeamDto dto,
            Authentication auth
    ) {
        return ResponseEntity.ok(teamService.createTeam(dto.getName(), auth.getName()));
    }

    @PostMapping("/register")
    @Operation(summary = "Iscrivi Team ad Hackathon", description = "Il Leader iscrive il proprio team a un Hackathon specifico (usando i nomi). Verifica che l'hackathon sia in fase di iscrizione.")
    public ResponseEntity<String> registerTeamByName(
            @RequestBody RegisterTeamByNameDto dto,
            Authentication auth
    ) {
        teamService.registerTeamByName(dto.getTeamName(), dto.getHackathonName(), auth.getName());

        return ResponseEntity.ok("Team " + dto.getTeamName() +
                " iscritto con successo a " + dto.getHackathonName() + "!");
    }

    // =================================================================================
    // SEZIONE 2: GESTIONE MEMBRI (Inviti - Lato Leader)
    // =================================================================================

    @PostMapping("/{teamId}/invite")
    @Operation(summary = "Invia Invito (Leader)", description = "Il Leader invia un invito via email a un altro utente per unirsi al team.")
    public ResponseEntity<String> inviteUser(
            @PathVariable Long teamId,
            @RequestBody inviteUserDto dto,
            Authentication auth
    ) {
        teamService.sendInvitation(teamId, dto.getUserEmail(), auth.getName());
        return ResponseEntity.ok("Invito inviato a " + dto.getUserEmail());
    }

    // =================================================================================
    // SEZIONE 3: GESTIONE PARTECIPAZIONE (Inviti - Lato Utente Invitato)
    // =================================================================================

    @GetMapping("/invitations")
    @Operation(summary = "I miei inviti in PENDING", description = "Visualizza tutti gli inviti ricevuti dall'utente loggato che sono ancora in stato PENDING.")
    public ResponseEntity<List<TeamInvitation>> getMyInvitations(Authentication auth) {
        return ResponseEntity.ok(teamService.getMyPendingInvitations(auth.getName()));
    }

    @PostMapping("/invitations/{invitationId}/accept")
    @Operation(summary = "Accetta Invito", description = "L'utente accetta di entrare nel team. Se il team è già iscritto a un hackathon, verifica che le iscrizioni siano aperte.")
    public ResponseEntity<String> acceptInvitation(
            @PathVariable Long invitationId,
            Authentication auth
    ) {
        teamService.acceptInvitation(invitationId, auth.getName());
        return ResponseEntity.ok("Invito accettato! Benvenuto nel team.");
    }

    @PostMapping("/invitations/{invitationId}/reject")
    @Operation(summary = "Rifiuta Invito", description = "L'utente rifiuta l'invito e questo viene chiuso.")
    public ResponseEntity<String> rejectInvitation(
            @PathVariable Long invitationId,
            Authentication auth
    ) {
        teamService.rejectInvitation(invitationId, auth.getName());
        return ResponseEntity.ok("Invito rifiutato correttamente.");
    }

    // =================================================================================
    // SEZIONE 4: GESTIONE LIFECYCLE (Visualizza, Esci, Espelli)
    // =================================================================================

    @GetMapping("/my-team")
    @Operation(summary = "Il mio Team", description = "Restituisce i dettagli del team di cui l'utente loggato fa parte.")
    public ResponseEntity<Team> getMyTeam(Authentication auth) {
        return ResponseEntity.ok(teamService.getMyTeam(auth.getName()));
    }

    @PostMapping("/leave")
    @Operation(summary = "Abbandona Team", description = "L'utente esce dal team corrente. Se è il Leader, il team viene sciolto.")
    public ResponseEntity<String> leaveTeam(Authentication auth) {
        teamService.leaveTeam(auth.getName());
        return ResponseEntity.ok("Hai abbandonato il team con successo.");
    }

    @DeleteMapping("/{teamId}/members/{memberId}")
    @Operation(summary = "Espelli Membro (Kick)", description = "Il Leader rimuove forzatamente un membro dal team.")
    public ResponseEntity<String> kickMember(
            @PathVariable Long teamId,
            @PathVariable Long memberId,
            Authentication auth
    ) {
        teamService.kickMember(teamId, memberId, auth.getName());
        return ResponseEntity.ok("Membro rimosso dal team.");
    }

    @DeleteMapping("/{teamId}")
    @Operation(summary = "Sciogli Team", description = "Il Leader elimina definitivamente il team.")
    public ResponseEntity<String> deleteTeam(
            @PathVariable Long teamId,
            Authentication auth
    ) {
        teamService.deleteTeam(teamId, auth.getName());
        return ResponseEntity.ok("Team sciolto con successo.");
    }
}