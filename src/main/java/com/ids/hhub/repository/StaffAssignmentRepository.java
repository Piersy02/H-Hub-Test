package com.ids.hhub.repository;

import com.ids.hhub.model.*;
import com.ids.hhub.model.enums.StaffRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffAssignmentRepository extends JpaRepository<StaffAssignment, Long> {
    // Trova ruolo di un utente in un hackathon specifico
    boolean existsByUserIdAndHackathonIdAndRole(Long userId, Long hackathonId, StaffRole role);

    // 2. Trova l'oggetto assegnazione completo (Utile per sapere CHE ruolo ha l'utente)
    // Restituisce Optional perché l'utente potrebbe non far parte dello staff di quell'hackathon
    Optional<StaffAssignment> findByUserIdAndHackathonId(Long userId, Long hackathonId);

    // Conta quanti staff con quel ruolo ci sono nell'hackathon
    long countByHackathonIdAndRole(Long hackathonId, StaffRole role);

    boolean existsByUserIdAndHackathonId(Long userId, Long hackathonId);
}
