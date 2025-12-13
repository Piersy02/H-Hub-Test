package com.ids.hhub.controller;

import com.ids.hhub.dto.AddStaffDto;
import com.ids.hhub.dto.CreateHackathonDto;
import com.ids.hhub.model.Hackathon;
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

    // 3. CREAZIONE (Solo Utenti Loggati)
    @PostMapping
    public ResponseEntity<Hackathon> create(
            @RequestBody CreateHackathonDto dto,
            Authentication authentication // Spring Security inietta l'utente loggato qui
    ) {
        // Recuperiamo l'email dell'utente loggato dal contesto di sicurezza
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
            Authentication authentication
    ) {
        String emailRichiedente = authentication.getName();

        hackathonService.addStaffMember(id, dto, emailRichiedente);

        return ResponseEntity.ok("Membro dello staff aggiunto con successo!");
    }
}
