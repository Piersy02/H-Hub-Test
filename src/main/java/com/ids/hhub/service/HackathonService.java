package com.ids.hhub.service;

import com.ids.hhub.dto.CreateHackathonDto;
import com.ids.hhub.model.*;
import com.ids.hhub.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HackathonService {

    @Autowired private HackathonRepository hackathonRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private StaffAssignmentRepository staffRepo;

    @Transactional
    public Hackathon createHackathon(CreateHackathonDto dto) {
        User organizer = userRepo.findById(dto.getOrganizerId())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        Hackathon h = new Hackathon();
        h.setName(dto.getName());
        h.setDescription(dto.getDescription());
        h.setStartDate(dto.getStartDate());
        h.setEndDate(dto.getEndDate());
        h.setPrizeAmount(dto.getPrizeAmount());
        h.setState(HackathonState.REGISTRATION_OPEN);

        h = hackathonRepo.save(h);

        // Assegna automaticamente il ruolo di ORGANIZER
        StaffAssignment assignment = new StaffAssignment(organizer, h, StaffRole.ORGANIZER);
        staffRepo.save(assignment);

        return h;
    }

    public List<Hackathon> getAllHackathons() {
        return hackathonRepo.findAll();
    }
}
