package com.ids.hhub.controller;

import com.ids.hhub.dto.ChangeRoleDto;
import com.ids.hhub.model.User;
import com.ids.hhub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    // Endpoint: PATCH /api/admin/users/{id}/role
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<String> changeRole(@PathVariable Long id, @RequestBody ChangeRoleDto dto) {

        User updatedUser = userService.changeUserRole(id, dto.getNewRole());

        return ResponseEntity.ok("Ruolo aggiornato con successo! L'utente " + updatedUser.getEmail() +
                " ora è " + updatedUser.getPlatformRole());
    }
}