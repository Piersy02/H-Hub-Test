package com.ids.hhub.controller;

import com.ids.hhub.dto.EvaluationDto;
import com.ids.hhub.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/evaluations")
@Tag(name = "Evaluation Management", description = "API per la gestione delle valutazioni da parte dei Giudici")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    // =================================================================================
    // SEZIONE 1: OPERAZIONI DI VOTO (Lato Giudice)
    // =================================================================================

    @PostMapping("/submission/{submissionId}")
    @Operation(summary = "Inserisci Valutazione", description = "Il Giudice assegna un punteggio (0-10) e un commento a una sottomissione. \n" +
            "Vincoli: \n" +
            "1. L'utente deve essere un GIUDICE assegnato a questo Hackathon.\n" +
            "2. L'Hackathon deve essere nello stato EVALUATION (State Pattern).")
    public ResponseEntity<String> evaluate(
            @PathVariable Long submissionId,
            @RequestBody EvaluationDto dto,
            Authentication auth
    ) {
        evaluationService.evaluateSubmission(
                submissionId,
                dto.getScore(),
                dto.getComment(),
                auth.getName()
        );

        return ResponseEntity.ok("Valutazione salvata con successo!");
    }
}