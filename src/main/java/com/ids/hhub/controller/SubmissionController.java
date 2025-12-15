package com.ids.hhub.controller;

import com.ids.hhub.dto.SubmissionDto;
import com.ids.hhub.model.Submission;
import com.ids.hhub.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    @Autowired private SubmissionService submissionService;

    // POST /api/submissions/team/{teamId}
    @PostMapping("/team/{teamId}")
    public ResponseEntity<Submission> submitProject(
            @PathVariable Long teamId,
            @RequestBody SubmissionDto dto,
            Authentication auth
    ) {
        // Il service controlla:
        // 1. Se sei membro del team
        // 2. Se l'Hackathon è in stato ONGOING (State Pattern)
        Submission sub = submissionService.submitProject(
                teamId,
                dto.getProjectUrl(),
                dto.getDescription(),
                auth.getName()
        );

        return ResponseEntity.ok(sub);
    }
}