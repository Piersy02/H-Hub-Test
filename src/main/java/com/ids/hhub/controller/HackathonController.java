package com.ids.hhub.controller;

import com.ids.hhub.dto.AddStaffDto;
import com.ids.hhub.dto.ChangeStatusDto;
import com.ids.hhub.dto.CreateHackathonDto;
import com.ids.hhub.model.Hackathon;
import com.ids.hhub.model.Team;
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
