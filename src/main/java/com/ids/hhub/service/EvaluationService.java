package com.ids.hhub.service;

import com.ids.hhub.model.StaffAssignment;
import com.ids.hhub.model.StaffRole;
import com.ids.hhub.model.Submission;
import com.ids.hhub.model.User;
import com.ids.hhub.repository.StaffAssignmentRepository;
import com.ids.hhub.repository.SubmissionRepository;
import com.ids.hhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    @Autowired
    private final UserRepository userRepo;
    private final SubmissionRepository submissionRepo;
    private final StaffAssignmentRepository staffRepo;

    public void evaluateSubmission(Long submissionId, int score, String comment, String judgeEmail) {
        // 1. Recupera dati
        User judge = userRepo.findByEmail(judgeEmail).orElseThrow();
        Submission submission = submissionRepo.findById(submissionId).orElseThrow();
        Long hackathonId = submission.getTeam().getHackathon().getId();

        // 2. CHECK DI SICUREZZA CONTESTUALE
        // "L'utente è JUDGE per l'hackathon di questa sottomissione?"
        boolean isJudge = staffRepo.existsByUserIdAndHackathonIdAndRole(
                judge.getId(),
                hackathonId,
                StaffRole.JUDGE
        );

        if (!isJudge) {
            throw new SecurityException("ACCESSO NEGATO: Non sei un giudice per questo hackathon.");
        }

        // 3. Logica di business
        // ... salva il voto ...
    }

}
