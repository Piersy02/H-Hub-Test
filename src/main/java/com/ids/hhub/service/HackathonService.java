package com.ids.hhub.service;

import com.ids.hhub.dto.*;
import com.ids.hhub.model.*;
import com.ids.hhub.model.enums.HackathonStatus;
import com.ids.hhub.model.enums.PlatformRole;
import com.ids.hhub.model.enums.StaffRole;
import com.ids.hhub.repository.*;
import com.ids.hhub.service.external.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        User organizer = getUserByEmail(organizerEmail);

        // Controllo specifico per la creazione (EVENT_CREATOR)
        if (organizer.getPlatformRole() != PlatformRole.EVENT_CREATOR
                && organizer.getPlatformRole() != PlatformRole.ADMIN) {
            throw new SecurityException("Non hai i permessi per creare un Hackathon. Richiedi l'upgrade a Event Creator.");
        }

        Hackathon h = mapDtoToEntity(dto);
        h = hackathonRepo.save(h);

        // Assegna Organizzatore
        saveStaffAssignment(organizer, h, StaffRole.ORGANIZER);

        // Assegna Giudice (Opzionale)
        if (dto.getJudgeId() != null) {
            if (dto.getJudgeId().equals(organizer.getId())) {
                throw new RuntimeException("L'organizzatore non può essere anche Giudice!");
            }
            User judge = getUserById(dto.getJudgeId());
            saveStaffAssignment(judge, h, StaffRole.JUDGE);
        }

        // Assegna Mentori (Opzionale)
        if (dto.getMentorIds() != null) {
            for (Long mentorId : dto.getMentorIds()) {
                if (mentorId.equals(organizer.getId())) continue;
                if (dto.getJudgeId() != null && mentorId.equals(dto.getJudgeId())) continue;

                User mentor = getUserById(mentorId);
                if (!staffRepo.existsByUserIdAndHackathonId(mentor.getId(), h.getId())) {
                    saveStaffAssignment(mentor, h, StaffRole.MENTOR);
                }
            }
        }
        return h;
    }

    // --- 2. GESTIONE STAFF ---
    @Transactional
    public void addStaffMember(Long hackathonId, AddStaffDto dto, String requesterEmail) {
        User requester = getUserByEmail(requesterEmail);

        // HELPER: Controllo permessi Organizzatore/Admin
        checkOrganizerOrAdminPermission(requester, hackathonId);

        Hackathon hackathon = getHackathonById(hackathonId);
        User targetUser = getUserById(dto.getUserId());

        // Verifica duplicati
        if (staffRepo.findByUserIdAndHackathonId(dto.getUserId(), hackathonId).isPresent()) {
            throw new RuntimeException("L'utente fa già parte dello staff! Non può avere due ruoli.");
        }

        // Vincolo Giudice Unico
        if (dto.getRole() == StaffRole.JUDGE) {
            if (staffRepo.countByHackathonIdAndRole(hackathonId, StaffRole.JUDGE) >= 1) {
                throw new RuntimeException("IMPOSSIBILE: Esiste già un Giudice per questo Hackathon.");
            }
        }

        saveStaffAssignment(targetUser, hackathon, dto.getRole());
    }

    // --- 3. CAMBIO STATO ---
    @Transactional
    public void changeHackathonStatus(Long hackathonId, HackathonStatus newStatus, String requesterEmail) {
        User requester = getUserByEmail(requesterEmail);
        checkOrganizerOrAdminPermission(requester, hackathonId);

        Hackathon h = getHackathonById(hackathonId);
        h.setStatus(newStatus);
        hackathonRepo.save(h);
        System.out.println("Stato Hackathon " + h.getId() + " cambiato in: " + newStatus);
    }

    // --- 4. CHIUSURA E VINCITORE ---
    @Transactional
    public Team proclaimWinner(Long hackathonId, String organizerEmail) {
        User organizer = getUserByEmail(organizerEmail);
        checkOrganizerOrAdminPermission(organizer, hackathonId);

        Hackathon h = getHackathonById(hackathonId);

        if (h.getStatus() != HackathonStatus.EVALUATION) {
            throw new IllegalStateException("L'hackathon deve essere in fase di valutazione per chiudere.");
        }

        Team winner = calculateWinner(h); // Ho estratto anche l'algoritmo per pulizia

        boolean paid = paymentService.processPayment(winner.getLeader().getEmail(), h.getPrizeAmount());
        if (!paid) throw new RuntimeException("Errore critico durante il pagamento!");

        h.setWinner(winner);
        h.setStatus(HackathonStatus.FINISHED);
        hackathonRepo.save(h);

        return winner;
    }

    // --- 5. REPORT VIOLAZIONI ---
    public List<ViolationReport> getViolationReports(Long hackathonId, String requesterEmail) {
        User requester = getUserByEmail(requesterEmail);
        checkOrganizerOrAdminPermission(requester, hackathonId);

        // Verifica esistenza hackathon
        if(!hackathonRepo.existsById(hackathonId)) throw new RuntimeException("Hackathon non trovato");

        return violationRepo.findByHackathonId(hackathonId);
    }

    // --- 6. SQUALIFICA ---
    @Transactional
    public void disqualifyTeam(Long hackathonId, Long teamId, String requesterEmail) {
        User requester = getUserByEmail(requesterEmail);
        checkOrganizerOrAdminPermission(requester, hackathonId);

        Team team = teamRepo.findById(teamId).orElseThrow(() -> new RuntimeException("Team non trovato"));

        if (team.getHackathon() == null || !team.getHackathon().getId().equals(hackathonId)) {
            throw new RuntimeException("Il team non è iscritto a questo hackathon.");
        }

        if (team.getSubmission() != null) {
            submissionRepo.delete(team.getSubmission());
            team.setSubmission(null);
        }
        team.setHackathon(null);
        teamRepo.save(team);
        System.out.println("Team " + team.getName() + " squalificato.");
    }

    // --- 7. VISUALIZZA SOTTOMISSIONI (Per Giudici/Staff) ---
    public List<Submission> getSubmissionsForHackathon(Long hackathonId, String requesterEmail) {
        User requester = getUserByEmail(requesterEmail);

        // HELPER: Controllo permessi Staff generico/Admin
        checkStaffOrAdminPermission(requester, hackathonId);

        Hackathon h = getHackathonById(hackathonId);
        List<Submission> submissions = new ArrayList<>();
        for (Team t : h.getTeams()) {
            if (t.getSubmission() != null) {
                submissions.add(t.getSubmission());
            }
        }
        return submissions;
    }

    // --- 8. LISTA TEAM ISCRITTI (Riservata Staff/Admin) ---
    public List<TeamSummaryDto> getRegisteredTeams(Long hackathonId, String requesterEmail) {
        User requester = getUserByEmail(requesterEmail);

        // 1. Controllo Permessi: Staff (qualsiasi ruolo) o Admin
        // Usiamo l'helper che abbiamo creato prima
        checkStaffOrAdminPermission(requester, hackathonId);

        // 2. Recupera l'Hackathon
        Hackathon h = getHackathonById(hackathonId);

        // 3. Mappatura Entity -> DTO
        List<TeamSummaryDto> result = new ArrayList<>();

        for (Team team : h.getTeams()) {
            String leaderEmail = "N/A";
            String leaderName = "N/A";

            if (team.getLeader() != null) {
                leaderEmail = team.getLeader().getEmail();
                leaderName = team.getLeader().getName() + " " + team.getLeader().getSurname();
            }

            result.add(new TeamSummaryDto(
                    team.getId(),
                    team.getName(),
                    leaderEmail,
                    leaderName
            ));
        }

        return result;
    }

    // METODI HELPER PRIVATI

    // Helper 1: Recupera Utente per Email
    private User getUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato: " + email));
    }

    // Helper 2: Recupera Utente per ID
    private User getUserById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Utente non trovato ID: " + id));
    }

    // Helper 3: Controllo Permessi ORGANIZZATORE o ADMIN
    private void checkOrganizerOrAdminPermission(User user, Long hackathonId) {
        boolean isOrganizer = staffRepo.existsByUserIdAndHackathonIdAndRole(
                user.getId(), hackathonId, StaffRole.ORGANIZER);
        boolean isAdmin = user.getPlatformRole() == PlatformRole.ADMIN;

        if (!isOrganizer && !isAdmin) {
            throw new SecurityException("Accesso Negato: Solo l'Organizzatore o l'Admin possono eseguire questa operazione.");
        }
    }

    // Helper 4: Controllo Permessi STAFF (Qualsiasi ruolo) o ADMIN
    private void checkStaffOrAdminPermission(User user, Long hackathonId) {
        boolean isStaff = staffRepo.existsByUserIdAndHackathonId(user.getId(), hackathonId);
        boolean isAdmin = user.getPlatformRole() == PlatformRole.ADMIN;

        if (!isStaff && !isAdmin) {
            throw new SecurityException("Accesso Negato: Solo lo Staff o l'Admin possono visualizzare questi dati.");
        }
    }

    // Helper 5: Salva assegnazione staff
    private void saveStaffAssignment(User user, Hackathon h, StaffRole role) {
        StaffAssignment assignment = new StaffAssignment(user, h, role);
        staffRepo.save(assignment);
    }

    // Helper 6: Calcolo Vincitore (Aggiornato con controllo "Tutti Giudicati")
    private Team calculateWinner(Hackathon h) {

        // --- 1. CONTROLLO PRELIMINARE: TUTTI GIUDICATI? ---
        for (Team team : h.getTeams()) {
            Submission sub = team.getSubmission();

            // Se sub è NULL (il team non ha consegnato), questo IF è FALSO.
            // Quindi il codice salta tutto e passa al prossimo team.
            // Se il team ha sottomesso un progetto...
            if (sub != null) {
                // ...ma la lista delle valutazioni è vuota
                if (sub.getEvaluations().isEmpty()) {
                    throw new IllegalStateException(
                            "Impossibile proclamare il vincitore: Il team '" + team.getName() +
                                    "' ha inviato un progetto ma non è stato ancora giudicato."
                    );
                }
            }
        }

        // --- 2. CALCOLO VINCITORE (Se siamo qui, tutti i progetti hanno un voto) ---
        Team winner = null;
        int maxScore = -1;

        boolean atLeastOneSubmission = false;

        for (Team team : h.getTeams()) {
            Submission sub = team.getSubmission();
            if (sub != null && !sub.getEvaluations().isEmpty()) {
                atLeastOneSubmission = true;
                Evaluation eval = sub.getEvaluations().get(0); // Prendiamo l'unico voto

                if (eval.getScore() > maxScore) {
                    maxScore = eval.getScore();
                    winner = team;
                }
                // Gestione pareggio? Per ora vince il primo trovato o l'ultimo,
                // se serve logica avanzata va aggiunta qui.
            }
        }

        if (!atLeastOneSubmission) {
            throw new RuntimeException("Nessun progetto sottomesso in questo Hackathon. Impossibile proclamare un vincitore.");
        }

        if (winner == null) {
            // Caso teorico quasi impossibile se atLeastOneSubmission è true, ma per sicurezza:
            throw new RuntimeException("Errore nel calcolo del punteggio.");
        }

        return winner;
    }

    // Helper 7: Mappatura DTO
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
        h.setStatus(HackathonStatus.REGISTRATION_OPEN);
        return h;
    }

    // Helper 8: Mappatura DTO
    private HackathonPublicDto mapToPublicDto(Hackathon h) {
        HackathonPublicDto dto = new HackathonPublicDto();
        dto.setId(h.getId());
        dto.setName(h.getName());
        dto.setDescription(h.getDescription());
        dto.setLocation(h.getLocation());
        dto.setRules(h.getRules());
        dto.setStatus(h.getStatus().toString());
        dto.setStartDate(h.getStartDate());
        dto.setEndDate(h.getEndDate());
        dto.setRegistrationDeadline(h.getRegistrationDeadline());
        dto.setMaxTeamSize(h.getMaxTeamSize());

        if (h.getWinner() != null) {
            dto.setWinnerTeamName(h.getWinner().getName());
        }
        return dto;
    }

    // Helper 9: Mappatura DTO
    private HackathonStaffDto mapToStaffDto(Hackathon h) {
        // Riutilizza il mapping pubblico per i campi base
        HackathonPublicDto publicDto = mapToPublicDto(h);

        HackathonStaffDto dto = new HackathonStaffDto();
        // Copia campi base
        dto.setId(publicDto.getId());
        dto.setName(publicDto.getName());
        dto.setDescription(publicDto.getDescription());
        dto.setLocation(publicDto.getLocation());
        dto.setRules(publicDto.getRules());
        dto.setStatus(publicDto.getStatus());
        dto.setStartDate(publicDto.getStartDate());
        dto.setEndDate(publicDto.getEndDate());
        dto.setRegistrationDeadline(publicDto.getRegistrationDeadline());
        dto.setMaxTeamSize(publicDto.getMaxTeamSize());
        dto.setWinnerTeamName(publicDto.getWinnerTeamName());

        // Aggiungi campi sensibili
        dto.setPrizeAmount(h.getPrizeAmount());
        dto.setFullStaffList(h.getStaff()); // Qui vedrà la lista completa
        dto.setTotalTeamsRegistered(h.getTeams().size());

        return dto;
    }

    // Metodi pubblici di lettura
    public Hackathon getHackathonById(Long id) {
        return hackathonRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Hackathon non trovato con ID: " + id));
    }

    // 1. PER TUTTI (Visitatori/Utenti) - Ritorna DTO Pubblico
    public HackathonPublicDto getHackathonPublicDetails(Long id) {
        Hackathon h = getHackathonById(id);
        return mapToPublicDto(h);
    }

    // 2. PER LO STAFF (Organizzatore/Giudice/Mentore) - Ritorna DTO Completo
    public HackathonStaffDto getHackathonStaffDetails(Long id, String requesterEmail) {
        Hackathon h = getHackathonById(id);
        User requester = getUserByEmail(requesterEmail);

        // CONTROLLO SICUREZZA:
        // L'utente deve essere STAFF di *questo* hackathon (o ADMIN)
        boolean isStaff = staffRepo.existsByUserIdAndHackathonId(requester.getId(), id);
        boolean isAdmin = requester.getPlatformRole() == PlatformRole.ADMIN;

        if (!isStaff && !isAdmin) {
            throw new SecurityException("Accesso Negato: Non fai parte dello staff di questo Hackathon.");
        }

        return mapToStaffDto(h);
    }

    // 3. LISTA PUBBLICA (Per la Home Page)
    public List<HackathonPublicDto> getAllPublicHackathons() {
        List<Hackathon> all = hackathonRepo.findAll();
        List<HackathonPublicDto> dtos = new ArrayList<>();
        for (Hackathon h : all) {
            dtos.add(mapToPublicDto(h));
        }
        return dtos;
    }

    // 4. LISTA "I MIEI HACKATHON DI STAFF" (Dashboard Staff)
    public List<HackathonStaffDto> getMyStaffHackathons(String userEmail) {
        User user = getUserByEmail(userEmail);
        List<HackathonStaffDto> dtos = new ArrayList<>();

        // Recupera tutti gli hackathon
        List<Hackathon> all = hackathonRepo.findAll();

        for (Hackathon h : all) {
            // Se sono staff di questo evento, lo aggiungo alla lista completa
            if (staffRepo.existsByUserIdAndHackathonId(user.getId(), h.getId())) {
                dtos.add(mapToStaffDto(h));
            }
        }
        return dtos;
    }




}