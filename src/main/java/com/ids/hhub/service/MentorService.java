package com.ids.hhub.service;

import com.ids.hhub.model.*;
import com.ids.hhub.model.enums.StaffRole;
import com.ids.hhub.repository.*;
import com.ids.hhub.service.external.CalendarService; // La tua interfaccia Strategy
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MentorService {

    @Autowired private SupportRequestRepository supportRepo;
    @Autowired private ViolationReportRepository violationRepo;
    @Autowired private TeamRepository teamRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private StaffAssignmentRepository staffRepo;

    @Autowired private CalendarService calendarService; // STRATEGY PATTERN

    // --- 1. IL TEAM CHIEDE AIUTO ---
    @Transactional
    public SupportRequest requestSupport(Long teamId, String description, String requesterEmail) {
        Team team = teamRepo.findById(teamId).orElseThrow(() -> new RuntimeException("Team non trovato"));
        User requester = userRepo.findByEmail(requesterEmail).orElseThrow();

        // Solo un membro del team può chiedere aiuto
        if (!team.getMembers().contains(requester)) {
            throw new SecurityException("Devi essere nel team per chiedere supporto.");
        }

        SupportRequest req = new SupportRequest(description, team, team.getHackathon());
        return supportRepo.save(req);
    }

    // --- 2. IL MENTORE VISUALIZZA LE RICHIESTE ---
    public List<SupportRequest> getRequestsForHackathon(Long hackathonId, String mentorEmail) {
        checkMentorRole(hackathonId, mentorEmail);
        return supportRepo.findByHackathonId(hackathonId);
    }

    // --- 3. IL MENTORE FISSA UNA CALL (Strategy Pattern) ---
    @Transactional
    public SupportRequest scheduleSupportCall(Long requestId, String dateTime, String mentorEmail) {
        SupportRequest req = supportRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Richiesta non trovata"));

        User mentor = checkMentorRole(req.getHackathon().getId(), mentorEmail);

        // USIAMO LA STRATEGIA CALENDAR
        String link = calendarService.scheduleMeeting(
                mentor.getEmail(),
                req.getTeam().getLeader().getEmail(),
                dateTime
        );

        // Aggiorniamo la richiesta
        req.setMeetingLink(link);
        req.setResolved(true); // Consideriamo la richiesta gestita

        return supportRepo.save(req);
    }

    // --- 4. IL MENTORE SEGNALA UNA VIOLAZIONE ---
    @Transactional
    public ViolationReport reportViolation(Long teamId, String description, String mentorEmail) {
        Team team = teamRepo.findById(teamId).orElseThrow();
        User mentor = checkMentorRole(team.getHackathon().getId(), mentorEmail);

        ViolationReport report = new ViolationReport(description, team, mentor, team.getHackathon());
        return violationRepo.save(report);
    }

    // --- Metodo Helper per i controlli di sicurezza ---
    private User checkMentorRole(Long hackathonId, String email) {
        User user = userRepo.findByEmail(email).orElseThrow();
        boolean isMentor = staffRepo.existsByUserIdAndHackathonIdAndRole(
                user.getId(), hackathonId, StaffRole.MENTOR);

        if (!isMentor) {
            throw new SecurityException("Accesso Negato: Non sei un Mentore per questo Hackathon.");
        }
        return user;
    }
}