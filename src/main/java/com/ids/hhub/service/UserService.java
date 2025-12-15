package com.ids.hhub.service;

import com.ids.hhub.model.enums.PlatformRole;
import com.ids.hhub.model.User;
import com.ids.hhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    public User changeUserRole(Long userId, PlatformRole newRole) {
        // 1. Cerca l'utente
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + userId));

        // 2. Cambia il ruolo
        user.setPlatformRole(newRole);

        // 3. Salva
        return userRepo.save(user);
    }
}