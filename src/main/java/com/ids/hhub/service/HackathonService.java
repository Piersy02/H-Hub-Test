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
    @Autowired private ViolationReportRepository violationRepo;
    @Autowired private SubmissionRepository submissionRepo;
    @Autowired private TeamRepository teamRepo;

    // --- 1. CREAZIONE HACKATHON ---
    @Transactional
    public Hackathon createHackathon(CreateHackathonDto dto, String organizerEmail) {
        // A. Recupera l'utente
        User organizer = userRepo.findByEmail(organizerEmail)
                .orElseThrow(() -> new RuntimeException("Utente loggato non trovato"));

        // B. Controllo Ruolo Piattaforma (Solo EVENT_CREATOR o ADMIN)
        if (organizer.getPlatformRole() != PlatformRole.EVENT_CREATOR
                && organizer.getPlatformRole() != PlatformRole.ADMIN) {
            throw new SecurityException("Non hai i permessi per creare un Hackathon. Richiedi l'upgrade a Event Creator.");
        }

        // C. Mappatura DTO -> Entity (Metodo privato helper)
        Hackathon h = mapDtoToEntity(dto);

        // D. Salvataggio iniziale (per generare l'ID)
        h = hackathonRepo.save(h);

        // E. Assegnazione automatica Ruolo ORGANIZER
        StaffAssignment assignment = new StaffAssignment(organizer, h, StaffRole.ORGANIZER);
        staffRepo.save(assignment);

        return h;
    }

    // --- 2. GESTIONE STAFF ---
    @Transactional
    public void addStaffMember(Long hackathonId, AddStaffDto dto, String requesterEmail) {
        User requester = userRepo.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Utente richiedente non trovato"));

        Hackathon hackathon = getHackathonById(hackathonId);

        // Controllo Permessi: Admin Globale O Organizzatore dell'evento
        boolean isAdmin = requester.getPlatformRole() == PlatformRole.ADMIN;
        boolean isOrganizerOfThisEvent = staffRepo.existsByUserIdAndHackathonIdAndRole(
                requester.getId(), hackathonId, StaffRole.ORGANIZER);

        if (!isAdmin && !isOrganizerOfThisEvent) {
            throw new SecurityException("NON AUTORIZZATO: Solo l'Admin o l'Organizzatore possono gestire lo staff.");
        }

        User targetUser = userRepo.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Utente target non trovato"));

        // Verifica se l'utente è già presente nello staff di questo hackathon (con QUALSIASI ruolo)
        if (staffRepo.findByUserIdAndHackathonId(dto.getUserId(), hackathonId).isPresent()) {
            throw new RuntimeException("L'utente fa già parte dello staff di questo hackathon! Non può avere due ruoli o essere aggiunto due volte.");
        }

        StaffAssignment assignment = new StaffAssignment(targetUser, hackathon, dto.getRole());
        staffRepo.save(assignment);
    }

    // --- 3. CAMBIO STATO (Manuale) ---
    @Transactional
    public void changeHackathonStatus(Long hackathonId, HackathonStatus newStatus, String requesterEmail) {
        Hackathon h = getHackathonById(hackathonId);
        User requester = userRepo.findByEmail(requesterEmail).orElseThrow();

        // Controllo Permessi
        boolean isOrganizer = staffRepo.existsByUserIdAndHackathonIdAndRole(
                requester.getId(), h.getId(), StaffRole.ORGANIZER);
        boolean isAdmin = requester.getPlatformRole() == PlatformRole.ADMIN;

        if (!isOrganizer && !isAdmin) {
            throw new SecurityException("Solo l'Organizzatore può cambiare lo stato dell'evento!");
        }

        h.setStatus(newStatus);
        hackathonRepo.save(h);
        System.out.println("Stato Hackathon " + h.getId() + " cambiato in: " + newStatus);
    }

    // --- 4. CHIUSURA E PROCLAMAZIONE VINCITORE ---
    @Transactional
    public Team proclaimWinner(Long hackathonId, String organizerEmail) {
        Hackathon h = getHackathonById(hackathonId);
        User organizer = userRepo.findByEmail(organizerEmail).orElseThrow();

        // Controllo Permessi
        boolean isOrganizer = staffRepo.existsByUserIdAndHackathonIdAndRole(
                organizer.getId(), h.getId(), StaffRole.ORGANIZER);
        if (!isOrganizer) throw new SecurityException("Solo l'organizzatore può chiudere l'evento.");

        // Controllo Stato
        if (h.getStatus() != HackathonStatus.EVALUATION) {
            throw new IllegalStateException("L'hackathon deve essere in fase di valutazione per chiudere.");
        }

        // Algoritmo Calcolo Vincitore (Media Voti)
        Team winner = null;
        double maxScore = -1.0;

        for (Team team : h.getTeams()) {
            Submission sub = team.getSubmission();
            if (sub != null && !sub.getEvaluations().isEmpty()) {
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
            throw new RuntimeException("Impossibile determinare un vincitore: nessuna valutazione trovata.");
        }

        // Pagamento Premio (Strategy Pattern)
        boolean paid = paymentService.processPayment(winner.getLeader().getEmail(), h.getPrizeAmount());
        if (!paid) {
            throw new RuntimeException("Errore critico durante il pagamento del premio!");
        }

        // Chiusura Evento
        h.setWinner(winner);
        h.setStatus(HackathonStatus.FINISHED);
        hackathonRepo.save(h);

        return winner;
    }

    public List<ViolationReport> getViolationReports(Long hackathonId, String requesterEmail) {
        // 1. Recupera Hackathon e Utente
        Hackathon h = hackathonRepo.findById(hackathonId)
                .orElseThrow(() -> new RuntimeException("Hackathon non trovato"));

        User requester = userRepo.findByEmail(requesterEmail).orElseThrow();

        // 2. Controllo Permessi: Sei l'Organizzatore o l'Admin?
        boolean isOrganizer = staffRepo.existsByUserIdAndHackathonIdAndRole(
                requester.getId(), h.getId(), StaffRole.ORGANIZER);
        boolean isAdmin = requester.getPlatformRole() == PlatformRole.ADMIN;

        if (!isOrganizer && !isAdmin) {
            throw new SecurityException("Solo l'Organizzatore può vedere le segnalazioni!");
        }

        // 3. Restituisci la lista
        return violationRepo.findByHackathonId(hackathonId);
    }

    @Transactional
    public void disqualifyTeam(Long hackathonId, Long teamId, String requesterEmail) {
        // 1. Recupera i dati
        Hackathon h = hackathonRepo.findById(hackathonId)
                .orElseThrow(() -> new RuntimeException("Hackathon non trovato"));

        Team team = teamRepo.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team non trovato"));

        User requester = userRepo.findByEmail(requesterEmail).orElseThrow();

        // 2. Controllo Permessi: Solo Organizzatore o Admin
        boolean isOrganizer = staffRepo.existsByUserIdAndHackathonIdAndRole(
                requester.getId(), h.getId(), StaffRole.ORGANIZER);
        boolean isAdmin = requester.getPlatformRole() == PlatformRole.ADMIN;

        if (!isOrganizer && !isAdmin) {
            throw new SecurityException("Solo l'Organizzatore può squalificare un team!");
        }

        // 3. Validazione: Il team è davvero iscritto a questo hackathon?
        if (team.getHackathon() == null || !team.getHackathon().getId().equals(hackathonId)) {
            throw new RuntimeException("Il team non è iscritto a questo hackathon (o è già stato rimosso).");
        }

        // 4. AZIONE DI SQUALIFICA

        // Opzionale: Cancelliamo la sottomissione se ne avevano fatta una
        if (team.getSubmission() != null) {
            submissionRepo.delete(team.getSubmission());
            team.setSubmission(null);
        }

        // Rimuoviamo il team dall'evento
        team.setHackathon(null);

        // Salviamo
        teamRepo.save(team);

        System.out.println("Team " + team.getName() + " squalificato dall'Hackathon " + h.getName());
    }

    // --- METODI DI LETTURA ---
    public Hackathon getHackathonById(Long id) {
        return hackathonRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Hackathon non trovato con ID: " + id));
    }

    public List<Hackathon> getAllHackathons() {
        return hackathonRepo.findAll();
    }

    // --- HELPER PRIVATO PER MAPPATURA ---
    private Hackathon mapDtoToEntity(CreateHackathonDto dto) {
        Hackathon h = new Hackathon();
        h.setName(dto.getName());
        h.setDescription(dto.getDescription());
        h.setRules(dto.getRules());
        h.setLocation(dto.getLocation());
        h.setRegistrationDeadline(dto.getRegistrationDeadline());
        h.setMaxTeamSize(dto.getMaxTeamSize());
        h.setStartDate(dto.getStartDate());
        h.setEndDate(dto.getEndDate());
        h.setPrizeAmount(dto.getPrizeAmount());

        // Stato di default
        h.setStatus(HackathonStatus.REGISTRATION_OPEN);
        return h;
    }
}