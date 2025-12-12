package com.ids.hhub.controller;

import com.ids.hhub.dto.LoginRequestDto;
import com.ids.hhub.dto.RegisterRequestDto;
import com.ids.hhub.model.User;
import com.ids.hhub.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody @Valid RegisterRequestDto request) {
        //passi il DTO al service
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid LoginRequestDto dto) {
        User user = authService.login(dto);
        // In un sistema reale qui restituiresti un Token (JWT).
        return ResponseEntity.ok("Login effettuato con successo! ID Utente: " + user.getId());
    }
}
