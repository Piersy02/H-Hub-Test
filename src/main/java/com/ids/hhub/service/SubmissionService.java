package com.ids.hhub.service;

import com.ids.hhub.model.*;
import com.ids.hhub.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepo; // Crealo (estende JpaRepository)
    @Autowired private TeamRepository teamRepo;
    @Autowired private UserRepository userRepo;

    @Transactional
    public Submission submitProject(Long teamId, String url, String desc, String requesterEmail) {
        Team team = teamRepo.findById(teamId).orElseThrow();
        User requester = userRepo.findByEmail(requesterEmail).orElseThrow();

        // 1. Solo il Leader (o un membro) può sottomettere
        if (!team.getMembers().contains(requester)) {
            throw new SecurityException("Devi essere membro del team per sottomettere!");
        }

        // CONTROLLO SCADENZA TEMPORALE
        Hackathon h = team.getHackathon();
        if (LocalDateTime.now().isAfter(h.getEndDate())) {
            throw new RuntimeException("Tempo scaduto! La deadline per la consegna è passata.");
        }

        // 2. STATE PATTERN CHECK
        // Lancia eccezione se non siamo in ONGOING
        team.getHackathon().getCurrentStateObject().submitProject(team.getHackathon(), team);

        // 3. Crea o Aggiorna
        Submission submission = team.getSubmission();
        if (submission == null) {
            submission = new Submission(url, desc, team);
        } else {
            submission.setProjectUrl(url);
            submission.setDescription(desc);
            submission.setSubmissionDate(LocalDateTime.now());
        }

        return submissionRepo.save(submission);
    }

    // --- NUOVO: VISUALIZZA SOTTOMISSIONE ---
    public Submission getSubmissionByTeamId(Long teamId, String requesterEmail) {
        Team team = teamRepo.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team non trovato"));

        User requester = userRepo.findByEmail(requesterEmail).orElseThrow();

        // Controllo: Sei membro del team?
        if (!team.getMembers().contains(requester)) {
            throw new SecurityException("Solo i membri del team possono vedere la propria sottomissione.");
        }

        if (team.getSubmission() == null) {
            throw new RuntimeException("Nessuna sottomissione trovata per questo team.");
        }

        return team.getSubmission();
    }

    // --- NUOVO: CANCELLA SOTTOMISSIONE ---
    @Transactional
    public void deleteSubmission(Long submissionId, String requesterEmail) {
        Submission sub = submissionRepo.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Sottomissione non trovata"));

        User requester = userRepo.findByEmail(requesterEmail).orElseThrow();
        Team team = sub.getTeam();

        // 1. Controllo: Sei membro del team?
        if (!team.getMembers().contains(requester)) {
            throw new SecurityException("Solo i membri del team possono cancellare la sottomissione.");
        }

        // 2. STATE PATTERN CHECK
        // Possiamo cancellare solo se siamo ancora in ONGOING.
        // Se siamo in EVALUATION o FINISHED, è troppo tardi.
        // Nota: Usiamo submitProject come proxy per verificare se è permesso modificare
        try {
            team.getHackathon().getCurrentStateObject().submitProject(team.getHackathon(), team);
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Impossibile ritirare il progetto: " + e.getMessage());
        }

        // 3. Cancellazione
        team.setSubmission(null); // Rimuovi riferimento lato Team
        submissionRepo.delete(sub); // Cancella dal DB
    }
}