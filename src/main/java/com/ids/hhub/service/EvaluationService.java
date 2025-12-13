package com.ids.hhub.service;

import com.ids.hhub.model.StaffAssignment;
import com.ids.hhub.model.StaffRole;
import com.ids.hhub.repository.StaffAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EvaluationService {

    @Autowired
    private StaffAssignmentRepository staffRepo;

    public void evaluateSubmission(Long userId, Long hackathonId, int score, String comment) {

        // 1. Verifica il ruolo: L'utente è un GIUDICE per QUESTO hackathon?
        Optional<StaffAssignment> assignment = staffRepo.findByUserIdAndHackathonId(userId, hackathonId);

        if (assignment.isEmpty() || assignment.get().getRole() != StaffRole.JUDGE) {
            throw new SecurityException("Accesso Negato: Non sei un giudice per questo hackathon!");
        }

        // 2. Se passa il controllo, procedi con la logica
        // ... salva la valutazione ...
    }
}
