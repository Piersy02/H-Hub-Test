package com.ids.hhub.service;

import com.ids.hhub.dto.UserSummaryDto;
import com.ids.hhub.model.enums.PlatformRole;
import com.ids.hhub.model.User;
import com.ids.hhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    // 1. LISTA DI TUTTI GLI UTENTI (Per Admin)
    public List<UserSummaryDto> getAllUsers() {
        return userRepo.findAll().stream()
                .map(u -> new UserSummaryDto(
                        u.getId(),
                        u.getName() + " " + u.getSurname(),
                        u.getEmail(),
                        u.getPlatformRole()
                ))
                .collect(Collectors.toList());
    }

    // 2. ELIMINA UTENTE (Ban)
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        // Controllo di sicurezza: Non cancellare se stessi o altri Admin (opzionale)
        if (user.getPlatformRole() == PlatformRole.ADMIN) {
            // throw new RuntimeException("Non puoi cancellare un amministratore.");
            // (Commentato: decidi tu se permetterlo)
        }

        // NOTA IMPORTANTE SULLE RELAZIONI:
        // Se l'utente è Leader di un team o Organizzatore, JPA potrebbe dare errore
        // se non hai impostato CascadeType.ALL o se non pulisci prima le relazioni.
        // Per l'esame, assicurati che User.java abbia le Cascade giuste o gestisci qui la pulizia.

        userRepo.delete(user);
    }

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