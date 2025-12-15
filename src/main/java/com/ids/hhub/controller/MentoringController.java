package com.ids.hhub.controller;

import com.ids.hhub.dto.MentoringSessionDto;
import com.ids.hhub.service.MentoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mentoring")
public class MentoringController {

    @Autowired private MentoringService mentoringService;

    // POST /api/mentoring/book
    @PostMapping("/book")
    public ResponseEntity<String> bookSession(
            @RequestBody MentoringSessionDto dto,
            Authentication auth
    ) {
        // Il service usa il CalendarService (Strategy Pattern)
        String meetingLink = mentoringService.bookMentoringSession(
                dto.getTeamId(),
                auth.getName(), // Email del Mentore
                dto.getDateTime()
        );

        return ResponseEntity.ok("Call prenotata! Ecco il link: " + meetingLink);
    }
}