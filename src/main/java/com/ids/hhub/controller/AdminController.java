package com.ids.hhub.controller;

import com.ids.hhub.dto.ChangeRoleDto;
import com.ids.hhub.dto.UserSummaryDto;
import com.ids.hhub.model.User;
import com.ids.hhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Management", description = "API riservate agli Amministratori di sistema per la gestione globale")
public class AdminController {

    @Autowired
    private UserService userService;

    // =================================================================================
    // SEZIONE 1: GESTIONE RUOLI E PERMESSI
    // =================================================================================

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Cambia Ruolo Piattaforma", description = "Permette all'Admin di modificare il ruolo globale di un utente (es. da USER a EVENT_CREATOR).")
    public ResponseEntity<String> changeRole(@PathVariable Long id, @RequestBody ChangeRoleDto dto) {
        User updatedUser = userService.changeUserRole(id, dto.getNewRole());
        return ResponseEntity.ok("Ruolo aggiornato con successo! L'utente " + updatedUser.getEmail() +
                " ora è " + updatedUser.getPlatformRole());
    }

    // =================================================================================
    // SEZIONE 2: GESTIONE UTENTI (Moderazione)
    // =================================================================================

    @GetMapping("/users")
    @Operation(summary = "Lista Utenti Piattaforma", description = "Visualizza l'elenco completo di tutti gli utenti registrati nel sistema.")
    public ResponseEntity<List<UserSummaryDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Elimina Utente (Ban)", description = "Rimuove definitivamente un utente dal sistema. Attenzione: questa operazione è irreversibile.")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Utente eliminato con successo.");
    }
}