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
}