package com.ids.hhub.controller;

import com.ids.hhub.dto.LoginRequestDto;
import com.ids.hhub.dto.RegisterRequestDto;
import com.ids.hhub.model.User;
import com.ids.hhub.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API per la gestione della registrazione e del login utenti")
public class AuthController {

    @Autowired
    private AuthService authService;

    // =================================================================================
    // SEZIONE 1: REGISTRAZIONE
    // =================================================================================

    @PostMapping("/register")
    @Operation(summary = "Registrazione Utente", description = "Crea un nuovo account utente nel sistema. \n" +
            "La password viene cifrata automaticamente. Di default viene assegnato il ruolo di piattaforma 'USER'.")
    public ResponseEntity<User> register(@RequestBody @Valid RegisterRequestDto request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // =================================================================================
    // SEZIONE 2: AUTENTICAZIONE
    // =================================================================================

    @PostMapping("/login")
    @Operation(summary = "Login Utente", description = "Verifica le credenziali (email e password). \n" +
            "Nota: Poiché il sistema usa Basic Auth, questo endpoint serve principalmente per verificare la correttezza delle credenziali e ottenere l'ID utente.")
    public ResponseEntity<String> login(@RequestBody @Valid LoginRequestDto dto) {
        User user = authService.login(dto);
        // In un sistema reale qui restituiresti un Token (JWT).
        return ResponseEntity.ok("Login effettuato con successo! ID Utente: " + user.getId());
    }
}