package com.ids.hhub.service;

import com.ids.hhub.dto.AddStaffDto;
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

    // --- CREATE (Aggiornato con email) ---
    @Transactional
    public Hackathon createHackathon(CreateHackathonDto dto, String organizerEmail) {
        User organizer = userRepo.findByEmail(organizerEmail)
                .orElseThrow(() -> new RuntimeException("Utente loggato non trovato"));

        // Qui potresti controllare: organizer.getPlatformRole() == PlatformRole.ADMIN?
        // O permettere a tutti gli utenti registrati di creare eventi.

        Hackathon h = new Hackathon();
        h.setName(dto.getName());
        h.setDescription(dto.getDescription());
        h.setStartDate(dto.getStartDate());
        h.setEndDate(dto.getEndDate());
        h.setPrizeAmount(dto.getPrizeAmount());
        h.setState(HackathonState.REGISTRATION_OPEN);

        h = hackathonRepo.save(h);

        // Assegna ruolo ORGANIZER
        StaffAssignment assignment = new StaffAssignment(organizer, h, StaffRole.ORGANIZER);
        staffRepo.save(assignment);

        return h;
    }

    // --- ADD STAFF ---
    @Transactional
    public void addStaffMember(Long hackathonId, AddStaffDto dto, String requesterEmail) {
        // 1. Trova l'utente che sta facendo la richiesta (deve essere l'organizzatore)
        User requester = userRepo.findByEmail(requesterEmail).orElseThrow();

        // 2. Verifica che il richiedente sia ORGANIZER per QUESTO hackathon
        boolean isOrganizer = staffRepo.existsByUserIdAndHackathonIdAndRole(
                requester.getId(), hackathonId, StaffRole.ORGANIZER);

        if (!isOrganizer) {
            throw new RuntimeException("Solo l'organizzatore può aggiungere staff!");
        }

        // 3. Trova l'utente da aggiungere come staff
        User newStaffUser = userRepo.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Utente da aggiungere non trovato"));

        Hackathon hackathon = getHackathonById(hackathonId);

        // 4. Salva l'assegnazione
        StaffAssignment assignment = new StaffAssignment(newStaffUser, hackathon, dto.getRole());
        staffRepo.save(assignment);
    }

    // --- GET BY ID ---
    public Hackathon getHackathonById(Long id) {
        return hackathonRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Hackathon non trovato con ID: " + id));
    }

    public List<Hackathon> getAllHackathons() {
        return hackathonRepo.findAll();
    }
}
