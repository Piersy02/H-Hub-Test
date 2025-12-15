package com.ids.hhub.service;

import com.ids.hhub.model.Evaluation;
import com.ids.hhub.model.Hackathon;
import com.ids.hhub.model.enums.StaffRole;
import com.ids.hhub.model.Submission;
import com.ids.hhub.model.User;
import com.ids.hhub.repository.EvaluationRepository;
import com.ids.hhub.repository.StaffAssignmentRepository;
import com.ids.hhub.repository.SubmissionRepository;
import com.ids.hhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EvaluationService {

    @Autowired private UserRepository userRepo;
    @Autowired private SubmissionRepository subRepo;
    @Autowired private StaffAssignmentRepository staffRepo;
    @Autowired private EvaluationRepository evalRepo;

    @Transactional
    public void evaluateSubmission(Long submissionId, int score, String comment, String judgeEmail) {
        // 1. Recupera dati
        User judge = userRepo.findByEmail(judgeEmail)
                .orElseThrow(() -> new RuntimeException("Giudice non trovato"));

        Submission sub = subRepo.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Sottomissione non trovata"));

        Hackathon hackathon = sub.getTeam().getHackathon();

        // 2. VALIDAZIONE INPUT (Requisito 0-10)
        if (score < 0 || score > 10) {
            throw new IllegalArgumentException("Il voto deve essere compreso tra 0 e 10.");
        }

        // 3. STATE PATTERN CHECK (Il cuore dell'esame)
        // Se l'hackathon non è in stato EVALUATION, questo metodo lancia eccezione
        hackathon.getCurrentStateObject().evaluateProject(hackathon);

        // 4. RUOLO CHECK (Sono un Giudice QUI?)
        boolean isJudge = staffRepo.existsByUserIdAndHackathonIdAndRole(
                judge.getId(), hackathon.getId(), StaffRole.JUDGE);

        if (!isJudge) {
            throw new SecurityException("Non sei un giudice per questo Hackathon!");
        }

        // 5. CONTROLLO DUPLICATI (Opzionale ma consigliato)
        // Evita che lo stesso giudice voti 10 volte lo stesso progetto
        boolean alreadyVoted = evalRepo.existsBySubmissionAndJudge(sub, judge);
        if (alreadyVoted) {
            throw new RuntimeException("Hai già valutato questo progetto!");
        }

        // 6. Salva
        Evaluation eval = new Evaluation();
        eval.setScore(score);
        eval.setComment(comment);
        eval.setSubmission(sub);
        eval.setJudge(judge);

        evalRepo.save(eval);
    }

}
