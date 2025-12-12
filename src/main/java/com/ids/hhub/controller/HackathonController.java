package com.ids.hhub.controller;

import com.ids.hhub.dto.CreateHackathonDto;
import com.ids.hhub.model.Hackathon;
import com.ids.hhub.service.HackathonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hackathons")
public class HackathonController {

    @Autowired
    private HackathonService hackathonService;

    // Accessibile a tutti (Visitor)
    @GetMapping
    public ResponseEntity<List<Hackathon>> getAll() {
        return ResponseEntity.ok(hackathonService.getAllHackathons());
    }

    // Accessibile solo a Staff/Utenti loggati (da proteggere con Spring Security)
    @PostMapping
    public ResponseEntity<Hackathon> create(@RequestBody CreateHackathonDto dto) {
        return ResponseEntity.ok(hackathonService.createHackathon(dto));
    }
}
