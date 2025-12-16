package com.ids.hhub.controller;

import com.ids.hhub.dto.*;
import com.ids.hhub.model.SupportRequest;
import com.ids.hhub.model.ViolationReport;
import com.ids.hhub.service.MentorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mentor")
public class MentorController {

    @Autowired private MentorService mentorService;

    // A. TEAM: Chiede aiuto
    @PostMapping("/request-support")
    public ResponseEntity<SupportRequest> askHelp(@RequestBody CreateSupportRequestDto dto, Authentication auth) {
        return ResponseEntity.ok(mentorService.requestSupport(dto.getTeamId(), dto.getDescription(), auth.getName()));
    }

    // B. MENTORE: Vede le richieste di un Hackathon
    @GetMapping("/requests/{hackathonId}")
    public ResponseEntity<List<SupportRequest>> getRequests(@PathVariable Long hackathonId, Authentication auth) {
        return ResponseEntity.ok(mentorService.getRequestsForHackathon(hackathonId, auth.getName()));
    }

    // C. MENTORE: Fissa la call (Usa Calendar)
    @PostMapping("/requests/{requestId}/schedule")
    public ResponseEntity<SupportRequest> scheduleCall(
            @PathVariable Long requestId,
            @RequestBody ScheduleCallDto dto,
            Authentication auth
    ) {
        return ResponseEntity.ok(mentorService.scheduleSupportCall(requestId, dto.getDateTime(), auth.getName()));
    }

    // D. MENTORE: Segnala violazione
    @PostMapping("/report-violation")
    public ResponseEntity<ViolationReport> report(@RequestBody ReportViolationDto dto, Authentication auth) {
        return ResponseEntity.ok(mentorService.reportViolation(dto.getTeamId(), dto.getDescription(), auth.getName()));
    }
}