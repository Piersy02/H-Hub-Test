package com.ids.hhub.controller;

import com.ids.hhub.dto.*;
import com.ids.hhub.model.Hackathon;
import com.ids.hhub.model.Submission;
import com.ids.hhub.model.Team;
import com.ids.hhub.model.ViolationReport;
import com.ids.hhub.service.HackathonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hackathons")
public class HackathonController {

    @Autowired
    private HackathonService hackathonService;

    // GET /api/hackathons/{id}/reports
    @GetMapping("/{id}/reports")
    public ResponseEntity<List<ViolationReport>> getReports(
            @PathVariable Long id,
            Authentication auth
    ) {
        return ResponseEntity.ok(hackathonService.getViolationReports(id, auth.getName()));
    }

    // 3. CREAZIONE (Solo Utenti Loggati che siano ADMIN o EVENT_CREATOR)
    @PostMapping
    public ResponseEntity<Hackathon> create(
            @RequestBody CreateHackathonDto dto,
            Authentication authentication // Spring Security inietta l'utente loggato qui
    ) {
        // 1. Prendi l'email dal token di login
        String emailOrganizer = authentication.getName();

        // Passiamo l'email al service invece dell'ID grezzo nel DTO
        return ResponseEntity.ok(hackathonService.createHackathon(dto, emailOrganizer));
    }

    // 4. AGGIUNTA STAFF (Solo Organizzatore)
    // Requisito: "L’Organizzatore può aggiungere più Mentori..."
    @PostMapping("/{id}/staff")
    public ResponseEntity<String> addStaff(
            @PathVariable Long id,
            @RequestBody AddStaffDto dto,
            Authentication authentication   // Spring Security ci dà chi è loggato
    ) {
        String emailRichiedente = authentication.getName();

        hackathonService.addStaffMember(id, dto, emailRichiedente);

        return ResponseEntity.ok("Membro dello staff aggiunto con successo!");
    }

    // POST /api/hackathons/{id}/close
    @PostMapping("/{id}/close")
    public ResponseEntity<String> closeHackathon(
            @PathVariable Long id,
            Authentication auth
    ) {
        Team winner = hackathonService.proclaimWinner(id, auth.getName());
        return ResponseEntity.ok("Hackathon concluso! Il vincitore è il team: " + winner.getName());
    }

    // POST /api/hackathons/{hackathonId}/teams/{teamId}/disqualify
    @PostMapping("/{hackathonId}/teams/{teamId}/disqualify")
    public ResponseEntity<String> disqualifyTeam(
            @PathVariable Long hackathonId,
            @PathVariable Long teamId,
            Authentication auth
    ) {
        hackathonService.disqualifyTeam(hackathonId, teamId, auth.getName());
        return ResponseEntity.ok("Team squalificato e rimosso dall'evento con successo.");
    }

    // PATCH /api/hackathons/{id}/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<String> changeStatus(
            @PathVariable Long id,
            @RequestBody ChangeStatusDto dto,
            Authentication auth
    ) {
        hackathonService.changeHackathonStatus(id, dto.getNewStatus(), auth.getName());
        return ResponseEntity.ok("Stato aggiornato con successo a " + dto.getNewStatus());
    }

    // GET /api/hackathons/{id}/submissions
    // Accessibile solo a Giudice e Organizzatore
    @GetMapping("/{id}/submissions")
    public ResponseEntity<List<Submission>> getSubmissionsForJudge(
            @PathVariable Long id,
            Authentication auth
    ) {
        // Deleghiamo al service (devi creare il metodo se non c'è)
        return ResponseEntity.ok(hackathonService.getSubmissionsForHackathon(id, auth.getName()));
    }

    // =================================================================================
    // 1. SCHEDA EVENTO (PUBBLICA)
    // =================================================================================
    // CHI LO USA: Visitatori (non loggati) e Utenti normali.
    // COSA FA: Mostra la "locandina" dell'evento.
    // DATI MOSTRATI: Nome, Descrizione, Date, Luogo.
    // DATI NASCOSTI: Budget, Regole interne, Email dello staff.
    @GetMapping("/{id}")
    public ResponseEntity<HackathonPublicDto> getHackathonPublicInfo(@PathVariable Long id) {
        return ResponseEntity.ok(hackathonService.getHackathonPublicDetails(id));
    }

    // =================================================================================
    // 2. DASHBOARD DI GESTIONE (RISERVATA ALLO STAFF)
    // =================================================================================
    // CHI LO USA: Solo l'Organizzatore, il Giudice o il Mentore di *questo* specifico evento.
    // COSA FA: Apre il pannello di controllo dell'evento.
    // DATI MOSTRATI: Tutto (incluso Budget, Regole complete, Lista completa dello staff).
    // SICUREZZA: Se provo ad accedere ma non sono staff, ricevo errore 403.
    @GetMapping("/{id}/dashboard")
    public ResponseEntity<HackathonStaffDto> getHackathonStaffInfo(
            @PathVariable Long id,
            Authentication auth
    ) {
        return ResponseEntity.ok(hackathonService.getHackathonStaffDetails(id, auth.getName()));
    }

    // =================================================================================
    // 3. HOME PAGE (LISTA HACKATHON)
    // =================================================================================
    // CHI LO USA: Chiunque apra la Home Page del sito.
    // COSA FA: Restituisce l'elenco di tutti gli hackathon disponibili nel sistema.
    // DATI MOSTRATI: Una lista di schede pubbliche (solo info base).
    @GetMapping
    public ResponseEntity<List<HackathonPublicDto>> getHomepageList() {
        return ResponseEntity.ok(hackathonService.getAllPublicHackathons());
    }

    // =================================================================================
    // 4. I MIEI LAVORI (AREA PERSONALE STAFF)
    // =================================================================================
    // CHI LO USA: Un utente loggato che fa parte dello staff in qualche evento.
    // COSA FA: Risponde alla domanda "In quali hackathon sto lavorando?".
    @GetMapping("/staff/me")
    public ResponseEntity<List<HackathonStaffDto>> getMyWorkingEvents(Authentication auth) {
        return ResponseEntity.ok(hackathonService.getMyStaffHackathons(auth.getName()));
    }
}
