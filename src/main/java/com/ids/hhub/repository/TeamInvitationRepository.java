package com.ids.hhub.repository;

import com.ids.hhub.model.TeamInvitation;
import com.ids.hhub.model.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, Long> {
    // Trova tutti gli inviti in attesa per un utente specifico
    List<TeamInvitation> findByInviteeEmailAndStatus(String email, InvitationStatus status);
}