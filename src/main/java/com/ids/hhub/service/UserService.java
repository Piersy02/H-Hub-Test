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

        // Controllo sicurezza: Non cancellare Admin (Opzionale)
        if (user.getPlatformRole() == PlatformRole.ADMIN) {
            // throw new RuntimeException("Non puoi cancellare un amministratore.");
        }

        // --- GESTIONE RELAZIONE CON IL TEAM ---
        if (user.getTeam() != null) {
            Team team = user.getTeam();

            // CASO A: L'utente è il LEADER -> Il Team viene sciolto
            if (team.getLeader().getId().equals(user.getId())) {

                // 1. Sgancia tutti gli altri membri dal team
                for (User member : team.getMembers()) {
                    member.setTeam(null);
                    // Non serve userRepo.save(member) qui se siamo in @Transactional,
                    // ma per sicurezza JPA lo fa in automatico al flush.
                }
                // Svuota la lista lato Java per evitare errori di concorrenza
                team.getMembers().clear();

                // 2. Cancella eventuale sottomissione
                if (team.getSubmission() != null) {
                    subRepo.delete(team.getSubmission());
                }

                // 3. Cancella il Team
                // Questo rimuoverà anche gli inviti grazie al Cascade (se impostato)
                // o dovrai cancellarli manualmente se hai errori di Foreign Key.
                teamRepo.delete(team);

                // 4. Importante: Setta a null il team dell'utente corrente
                // per dire a Hibernate "questo utente non ha più legami"
                user.setTeam(null);

            } else {
                // CASO B: L'utente è un MEMBRO SEMPLICE -> Esce dal team

                // 1. Rimuovi l'utente dalla lista del team
                team.getMembers().remove(user);

                // 2. Rimuovi il team dall'utente
                user.setTeam(null);

                // 3. Salva il team aggiornato
                teamRepo.save(team);
            }
        }

        // --- GESTIONE STAFF ASSIGNMENTS ---
        // Se User ha @OneToMany(cascade = CascadeType.ALL) su staffAssignments,
        // verranno cancellati da soli. Altrimenti dovresti cancellarli qui.
        // Assumiamo che User.java sia configurato bene.

        // ORA POSSIAMO CANCELLARE L'UTENTE
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