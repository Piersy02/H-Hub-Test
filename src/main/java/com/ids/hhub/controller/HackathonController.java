package com.ids.hhub.controller;

import com.ids.hhub.dto.AddStaffDto;
import com.ids.hhub.dto.ChangeStatusDto;
import com.ids.hhub.dto.CreateHackathonDto;
import com.ids.hhub.model.Hackathon;
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

    // 1. LISTA COMPLETA (Pubblico)
    @GetMapping
    public ResponseEntity<List<Hackathon>> getAll() {
        return ResponseEntity.ok(hackathonService.getAllHackathons());
    }

    // GET /api/hackathons/{id}/reports
    @GetMapping("/{id}/reports")
    public ResponseEntity<List<ViolationReport>> getReports(
            @PathVariable Long id,
            Authentication auth
    ) {
        return ResponseEntity.ok(hackathonService.getViolationReports(id, auth.getName()));
    }

    // 2. DETTAGLIO SINGOLO HACKATHON (Pubblico)
    // Serve quando un visitatore clicca su una card per leggere il regolamento
    @GetMapping("/{id}")
    public ResponseEntity<Hackathon> getById(@PathVariable Long id) {
        return ResponseEntity.ok(hackathonService.getHackathonById(id));
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
}
