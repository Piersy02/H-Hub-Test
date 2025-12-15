package com.ids.hhub.service;

import com.ids.hhub.model.Hackathon;
import com.ids.hhub.model.Team;
import com.ids.hhub.model.User;
import com.ids.hhub.model.enums.StaffRole;
import com.ids.hhub.repository.StaffAssignmentRepository;
import com.ids.hhub.repository.TeamRepository;
import com.ids.hhub.repository.UserRepository;
import com.ids.hhub.service.external.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MentoringService {

    // Spring inietta l'implementazione disponibile
    @Autowired
    private CalendarService calendarService;

    @Autowired
    private StaffAssignmentRepository staffRepo;
    @Autowired private TeamRepository teamRepo;
    @Autowired private UserRepository userRepo;

    public String bookMentoringSession(Long teamId, String mentorEmail, String dateTime) {
        Team team = teamRepo.findById(teamId).orElseThrow();
        User mentor = userRepo.findByEmail(mentorEmail).orElseThrow();
        Hackathon h = team.getHackathon();

        // 1. Verifica che sia un Mentore
        boolean isMentor = staffRepo.existsByUserIdAndHackathonIdAndRole(
                mentor.getId(), h.getId(), StaffRole.MENTOR);

        if (!isMentor) {
            throw new SecurityException("Non sei un mentore per questo Hackathon!");
        }

        // 2. USA IL PATTERN STRATEGY
        // Deleghiamo la creazione del link al servizio esterno
        String meetingLink = calendarService.scheduleMeeting(mentor.getEmail(), team.getLeader().getEmail(), dateTime);

        return meetingLink;
    }
}
