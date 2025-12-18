package com.ids.hhub.controller;

import com.ids.hhub.dto.SubmissionDto;
import com.ids.hhub.model.Submission;
import com.ids.hhub.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submissions")
@Tag(name = "Submission Management", description = "Gestione delle consegne dei progetti da parte dei Team")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    // =================================================================================
    // GESTIONE SOTTOMISSIONI (Team)
    // =================================================================================

    @PostMapping("/team/{teamId}")
    @Operation(summary = "Invia o Aggiorna Sottomissione", description = "Permette a un membro del team di caricare il progetto. Se esiste già, viene aggiornata. Richiede che l'Hackathon sia in stato ONGOING.")
    public ResponseEntity<Submission> submitProject(
            @PathVariable Long teamId,
            @RequestBody SubmissionDto dto,
            Authentication auth
    ) {
        Submission sub = submissionService.submitProject(
                teamId,
                dto.getProjectUrl(),
                dto.getDescription(),
                auth.getName()
        );
        return ResponseEntity.ok(sub);
    }

    @GetMapping("/team/{teamId}")
    @Operation(summary = "Visualizza la mia Sottomissione", description = "Permette ai membri del team di vedere cosa hanno caricato.")
    public ResponseEntity<Submission> getMySubmission(
            @PathVariable Long teamId,
            Authentication auth
    ) {
        return ResponseEntity.ok(submissionService.getSubmissionByTeamId(teamId, auth.getName()));
    }

    @DeleteMapping("/{submissionId}")
    @Operation(summary = "Ritira Sottomissione", description = "Permette al team di cancellare la sottomissione (solo se l'Hackathon è ancora in corso).")
    public ResponseEntity<String> deleteSubmission(
            @PathVariable Long submissionId,
            Authentication auth
    ) {
        submissionService.deleteSubmission(submissionId, auth.getName());
        return ResponseEntity.ok("Sottomissione ritirata con successo.");
    }
}