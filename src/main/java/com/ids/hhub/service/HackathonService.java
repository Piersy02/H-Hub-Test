package com.ids.hhub.service;

import com.ids.hhub.dto.AddStaffDto;
import com.ids.hhub.dto.CreateHackathonDto;
import com.ids.hhub.model.*;
import com.ids.hhub.model.enums.HackathonStatus;
import com.ids.hhub.model.enums.PlatformRole;
import com.ids.hhub.model.enums.StaffRole;
import com.ids.hhub.repository.*;
import com.ids.hhub.service.external.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HackathonService {

    @Autowired private HackathonRepository hackathonRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private StaffAssignmentRepository staffRepo;
    @Autowired private PaymentService paymentService;

    // --- CREATE HACKATHON ---
    @Transactional
    public Hackathon createHackathon(CreateHackathonDto dto, String organizerEmail) {
        // 1. Recupera l'utente che vuole creare l'evento
        User organizer = userRepo.findByEmail(organizerEmail)
                .orElseThrow(() -> new RuntimeException("Utente loggato non trovato"));

        // 2. CONTROLLO RUOLO DI PIATTAFORMA
        // Solo chi è EVENT_CREATOR (o ADMIN) può generare nuovi eventi.
        // Nota: Ho corretto la variabile da 'creator' a 'organizer'
        if (organizer.getPlatformRole() != PlatformRole.EVENT_CREATOR
                && organizer.getPlatformRole() != PlatformRole.ADMIN) {
            throw new SecurityException("Non hai i permessi per creare un Hackathon. Richiedi l'upgrade a Event Creator.");
        }

        // 3. Crea l'entità Hackathon
        Hackathon h = new Hackathon();
        h.setName(dto.getName());
        h.setDescription(dto.getDescription());
        h.setRules(dto.getRules());
        h.setRegistrationDeadline(dto.getRegistrationDeadline());
        h.setMaxTeamSize(dto.getMaxTeamSize());
        h.setStartDate(dto.getStartDate());
        h.setEndDate(dto.getEndDate());
        h.setPrizeAmount(dto.getPrizeAmount());

        // Imposta lo stato iniziale
        h.setStatus(HackathonStatus.REGISTRATION_OPEN);

        // Salva per generare l'ID
        h = hackathonRepo.save(h);

        // 4. ASSEGNAZIONE RUOLO CONTESTUALE
        // L'utente diventa automaticamente ORGANIZER di QUESTO specifico hackathon
        StaffAssignment assignment = new StaffAssignment(organizer, h, StaffRole.ORGANIZER);
        staffRepo.save(assignment);

        return h;
    }

    // --- ADD STAFF MEMBER ---
    @Transactional
    public void addStaffMember(Long hackathonId, AddStaffDto dto, String requesterEmail) {
        // 1. Chi sta facendo la richiesta?
        User requester = userRepo.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Utente richiedente non trovato"));

        // 2. Recupera l'Hackathon
        Hackathon hackathon = hackathonRepo.findById(hackathonId)
                .orElseThrow(() -> new RuntimeException("Hackathon non trovato"));

        // 3. CONTROLLO PERMESSI (Admin Globale O Organizzatore dell'evento)
        boolean isAdmin = requester.getPlatformRole() == PlatformRole.ADMIN;

        // Verifica se il richiedente è ORGANIZER per QUESTO specifico hackathon
        boolean isOrganizerOfThisEvent = staffRepo.existsByUserIdAndHackathonIdAndRole(
                requester.getId(), hackathonId, StaffRole.ORGANIZER);

        if (!isAdmin && !isOrganizerOfThisEvent) {
            throw new SecurityException("NON AUTORIZZATO: Solo l'Admin o l'Organizzatore possono gestire lo staff.");
        }

        // 4. Recupera l'utente da promuovere a staff
        User targetUser = userRepo.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Utente target non trovato"));

        // 5. Evita duplicati (Se è già staff con quel ruolo, errore)
        if (staffRepo.existsByUserIdAndHackathonIdAndRole(targetUser.getId(), hackathonId, dto.getRole())) {
            throw new RuntimeException("L'utente ha già questo ruolo in questo hackathon!");
        }

        // 6. Salva la nuova assegnazione
        StaffAssignment assignment = new StaffAssignment(targetUser, hackathon, dto.getRole());
        staffRepo.save(assignment);
    }

    // --- GET BY ID ---
    public Hackathon getHackathonById(Long id) {
        return hackathonRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Hackathon non trovato con ID: " + id));
    }

    // --- GET ALL ---
    public List<Hackathon> getAllHackathons() {
        return hackathonRepo.findAll();
    }

    @Transactional
    public Team proclaimWinner(Long hackathonId, String organizerEmail) {
        // 1. Recupera Hackathon e Organizzatore
        Hackathon h = hackathonRepo.findById(hackathonId).orElseThrow();
        User organizer = userRepo.findByEmail(organizerEmail).orElseThrow();

        // 2. Controllo Permessi (Solo Organizzatore)
        boolean isOrganizer = staffRepo.existsByUserIdAndHackathonIdAndRole(
                organizer.getId(), h.getId(), StaffRole.ORGANIZER);
        if (!isOrganizer) throw new SecurityException("Solo l'organizzatore può chiudere l'evento.");

        // 3. Controllo Stato (Deve essere in Valutazione)
        if (h.getStatus() != HackathonStatus.EVALUATION) {
            throw new IllegalStateException("L'hackathon deve essere in fase di valutazione per chiudere.");
        }

        // 4. ALGORITMO CALCOLO VINCITORE
        Team winner = null;
        double maxScore = -1.0;

        for (Team team : h.getTeams()) {
            Submission sub = team.getSubmission();
            if (sub != null && !sub.getEvaluations().isEmpty()) {
                // Calcola media voti
                double avg = sub.getEvaluations().stream()
                        .mapToInt(Evaluation::getScore)
                        .average()
                        .orElse(0.0);

                if (avg > maxScore) {
                    maxScore = avg;
                    winner = team;
                }
            }
        }

        if (winner == null) {
            throw new RuntimeException("Nessun vincitore calcolabile (forse nessuna valutazione?)");
        }

        // 5. PAGAMENTO (Strategy Pattern)
        boolean paid = paymentService.processPayment(winner.getLeader().getEmail(), h.getPrizeAmount());
        if (!paid) {
            throw new RuntimeException("Errore nel pagamento del premio!");
        }

        // 6. CHIUSURA
        h.setWinner(winner);
        h.setStatus(HackathonStatus.FINISHED); // Cambia stato nel DB
        hackathonRepo.save(h);

        return winner;
    }

}