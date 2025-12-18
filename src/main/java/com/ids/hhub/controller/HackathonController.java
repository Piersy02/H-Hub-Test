package com.ids.hhub.controller;

import com.ids.hhub.dto.*;
import com.ids.hhub.model.Hackathon;
import com.ids.hhub.model.Submission;
import com.ids.hhub.model.Team;
import com.ids.hhub.model.ViolationReport;
import com.ids.hhub.service.HackathonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hackathons")
@Tag(name = "Hackathon Management", description = "API per la gestione del ciclo di vita degli Hackathon")
public class HackathonController {

    @Autowired
    private HackathonService hackathonService;

    // =================================================================================
    // SEZIONE 1: ENDPOINT PUBBLICI (Accessibili a tutti)
    // =================================================================================

    @GetMapping
    @Operation(summary = "Lista pubblica Hackathon", description = "Restituisce l'elenco di tutti gli hackathon con le sole informazioni pubbliche.")
    public ResponseEntity<List<HackathonPublicDto>> getHomepageList() {
        return ResponseEntity.ok(hackathonService.getAllPublicHackathons());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dettaglio pubblico Hackathon", description = "Restituisce le informazioni pubbliche di un singolo hackathon.")
    public ResponseEntity<HackathonPublicDto> getHackathonPublicInfo(@PathVariable Long id) {
        return ResponseEntity.ok(hackathonService.getHackathonPublicDetails(id));
    }

    // =================================================================================
    // SEZIONE 2: CREAZIONE (Solo EVENT_CREATOR o ADMIN)
    // =================================================================================

    @PostMapping
    @Operation(summary = "Crea Hackathon", description = "Permette a un Event Creator o Admin di creare un nuovo evento. Chi crea diventa automaticamente Organizzatore.")
    public ResponseEntity<Hackathon> create(
            @RequestBody CreateHackathonDto dto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(hackathonService.createHackathon(dto, authentication.getName()));
    }

    // =================================================================================
    // SEZIONE 3: GESTIONE ORGANIZZATORE (Solo ORGANIZER o ADMIN)
    // =================================================================================

    @PostMapping("/{id}/staff")
    @Operation(summary = "Aggiungi Staff", description = "L'Organizzatore assegna ruoli (Mentore, Giudice, Co-Organizzatore) ad altri utenti.")
    public ResponseEntity<String> addStaff(
            @PathVariable Long id,
            @RequestBody AddStaffDto dto,
            Authentication authentication
    ) {
        hackathonService.addStaffMember(id, dto, authentication.getName());
        return ResponseEntity.ok("Membro dello staff aggiunto con successo!");
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambia Stato", description = "Modifica lo stato dell'Hackathon.")
    public ResponseEntity<String> changeStatus(
            @PathVariable Long id,
            @RequestBody ChangeStatusDto dto,
            Authentication auth
    ) {
        hackathonService.changeHackathonStatus(id, dto.getNewStatus(), auth.getName());
        return ResponseEntity.ok("Stato aggiornato con successo a " + dto.getNewStatus());
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Chiudi Hackathon e Proclama Vincitore", description = "Calcola il vincitore in base ai voti, effettua il pagamento e chiude l'evento.")
    public ResponseEntity<String> closeHackathon(
            @PathVariable Long id,
            Authentication auth
    ) {
        Team winner = hackathonService.proclaimWinner(id, auth.getName());
        return ResponseEntity.ok("Hackathon concluso! Il vincitore è il team: " + winner.getName());
    }

    @PostMapping("/{hackathonId}/teams/{teamId}/disqualify")
    @Operation(summary = "Squalifica Team", description = "Rimuove forzatamente un team dall'hackathon.")
    public ResponseEntity<String> disqualifyTeam(
            @PathVariable Long hackathonId,
            @PathVariable Long teamId,
            Authentication auth
    ) {
        hackathonService.disqualifyTeam(hackathonId, teamId, auth.getName());
        return ResponseEntity.ok("Team squalificato e rimosso dall'evento con successo.");
    }

    @GetMapping("/{id}/reports")
    @Operation(summary = "Visualizza Segnalazioni", description = "L'Organizzatore visualizza le violazioni segnalate dai Mentori.")
    public ResponseEntity<List<ViolationReport>> getReports(
            @PathVariable Long id,
            Authentication auth
    ) {
        return ResponseEntity.ok(hackathonService.getViolationReports(id, auth.getName()));
    }

    // =================================================================================
    // SEZIONE 4: DASHBOARD E OPERATIVITÀ STAFF (Giudici, Mentori, Org)
    // =================================================================================

    @GetMapping("/{id}/dashboard")
    @Operation(summary = "Dashboard Staff", description = "Restituisce tutte le informazioni (incluse quelle sensibili) per lo staff dell'evento singolo.")
    public ResponseEntity<HackathonStaffDto> getHackathonStaffInfo(
            @PathVariable Long id,
            Authentication auth
    ) {
        return ResponseEntity.ok(hackathonService.getHackathonStaffDetails(id, auth.getName()));
    }

    @GetMapping("/{id}/teams")
    @Operation(summary = "Lista Team Iscritti (Staff)", description = "Restituisce la lista dei team con email del leader. Accessibile solo allo Staff dell'evento o Admin.")
    public ResponseEntity<List<TeamSummaryDto>> getRegisteredTeams(
            @PathVariable Long id,
            Authentication auth
    ) {
        return ResponseEntity.ok(hackathonService.getRegisteredTeams(id, auth.getName()));
    }

    @GetMapping("/staff/me")
    @Operation(summary = "I miei eventi Staff", description = "Restituisce la lista degli hackathon in cui l'utente loggato ha un ruolo di staff.")
    public ResponseEntity<List<HackathonStaffDto>> getMyWorkingEvents(Authentication auth) {
        return ResponseEntity.ok(hackathonService.getMyStaffHackathons(auth.getName()));
    }

    @GetMapping("/{id}/submissions")
    @Operation(summary = "Visualizza Sottomissioni", description = "Permette ai membri dello staff di vedere i progetti consegnati dai team.")
    public ResponseEntity<List<Submission>> getSubmissionsForJudge(
            @PathVariable Long id,
            Authentication auth
    ) {
        return ResponseEntity.ok(hackathonService.getSubmissionsForHackathon(id, auth.getName()));
    }
}