package com.ids.hhub.controller;

import com.ids.hhub.dto.*;
import com.ids.hhub.model.SupportRequest;
import com.ids.hhub.model.ViolationReport;
import com.ids.hhub.service.MentorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mentor")
@Tag(name = "Mentor & Support Management", description = "API per la gestione del supporto ai team e le operazioni dei Mentori")
public class MentorController {

    @Autowired
    private MentorService mentorService;

    // =================================================================================
    // SEZIONE 1: SUPPORTO AI TEAM (Lato Team)
    // =================================================================================

    @PostMapping("/request-support")
    @Operation(summary = "Richiedi Supporto", description = "Un membro del team invia una richiesta di aiuto ai mentori dell'hackathon (es. problema tecnico).")
    public ResponseEntity<SupportRequest> askHelp(
            @RequestBody CreateSupportRequestDto dto,
            Authentication auth
    ) {
        return ResponseEntity.ok(mentorService.requestSupport(dto.getTeamId(), dto.getDescription(), auth.getName()));
    }

    // =================================================================================
    // SEZIONE 2: GESTIONE RICHIESTE (Lato Mentore)
    // =================================================================================

    @GetMapping("/requests/{hackathonId}")
    @Operation(summary = "Visualizza Richieste Pendenti", description = "Il Mentore visualizza la lista delle richieste di supporto inviate dai team per uno specifico hackathon. Richiede ruolo MENTOR.")
    public ResponseEntity<List<SupportRequest>> getRequests(
            @PathVariable Long hackathonId,
            Authentication auth
    ) {
        return ResponseEntity.ok(mentorService.getRequestsForHackathon(hackathonId, auth.getName()));
    }

    @PostMapping("/requests/{requestId}/schedule")
    @Operation(summary = "Fissa Call (Calendar Strategy)", description = "Il Mentore accetta una richiesta e fissa un orario. Il sistema usa il Calendar Service (Strategy Pattern) per generare il link del meeting.")
    public ResponseEntity<SupportRequest> scheduleCall(
            @PathVariable Long requestId,
            @RequestBody ScheduleCallDto dto,
            Authentication auth
    ) {
        return ResponseEntity.ok(mentorService.scheduleSupportCall(requestId, dto.getDateTime(), auth.getName()));
    }

    // =================================================================================
    // SEZIONE 3: DISCIPLINA E SEGNALAZIONI (Lato Mentore)
    // =================================================================================

    @PostMapping("/report-violation")
    @Operation(summary = "Segnala Violazione", description = "Il Mentore segnala un team all'Organizzatore per una presunta violazione del regolamento (es. plagio).")
    public ResponseEntity<ViolationReport> report(
            @RequestBody ReportViolationDto dto,
            Authentication auth
    ) {
        return ResponseEntity.ok(mentorService.reportViolation(dto.getTeamId(), dto.getDescription(), auth.getName()));
    }
}