package com.ids.hhub.service;

import com.ids.hhub.dto.UserSummaryDto;
import com.ids.hhub.model.Team;
import com.ids.hhub.model.enums.PlatformRole;
import com.ids.hhub.model.User;
import com.ids.hhub.repository.SubmissionRepository;
import com.ids.hhub.repository.TeamRepository;
import com.ids.hhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired private UserRepository userRepo;
    @Autowired private TeamRepository teamRepo;
    @Autowired private SubmissionRepository subRepo;

    // 1. LISTA DI TUTTI GLI UTENTI (Per Admin)
    public List<UserSummaryDto> getAllUsers() {
        return userRepo.findAll().stream()
                .map(u -> {
                    // Logica per determinare Team e Leadership
                    String teamName = "Nessuno"; // Valore di default
                    boolean isLeader = false;

                    if (u.getTeam() != null) {
                        teamName = u.getTeam().getName();

                        // Controllo se l'utente è il leader del suo team
                        // (Confrontiamo gli ID per sicurezza)
                        if (u.getTeam().getLeader() != null &&
                                u.getTeam().getLeader().getId().equals(u.getId())) {
                            isLeader = true;
                        }
                    }

                    return new UserSummaryDto(
                            u.getId(),
                            u.getName() + " " + u.getSurname(),
                            u.getEmail(),
                            u.getPlatformRole(),
                            teamName,    // <--- Passiamo il nome
                            isLeader     // <--- Passiamo il boolean
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        // --- 1. CONTROLLO DI SICUREZZA (NUOVO) ---
        // Se l'utente target è un ADMIN, blocca tutto.
        // Questo impedisce sia di cancellare se stessi (se si è admin), sia altri admin.
        if (user.getPlatformRole() == PlatformRole.ADMIN) {
            throw new SecurityException("OPERAZIONE NEGATA: Non è possibile eliminare un Amministratore (te stesso o altri).");
        }

        // --- 2. GESTIONE RELAZIONE CON IL TEAM
        if (user.getTeam() != null) {
            Team team = user.getTeam();

            // CASO A: L'utente è il LEADER -> Il Team viene sciolto
            if (team.getLeader().getId().equals(user.getId())) {

                // Sgancia tutti gli altri membri
                for (User member : team.getMembers()) {
                    member.setTeam(null);
                }
                team.getMembers().clear();

                // Cancella eventuale sottomissione
                if (team.getSubmission() != null) {
                    subRepo.delete(team.getSubmission());
                }

                // Cancella il Team
                teamRepo.delete(team);

                // Sgancia l'utente corrente
                user.setTeam(null);

            } else {
                // CASO B: L'utente è un MEMBRO SEMPLICE -> Esce dal team
                team.getMembers().remove(user);
                user.setTeam(null);
                teamRepo.save(team);
            }
        }

        // --- 3. CANCELLAZIONE EFFETTIVA ---
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