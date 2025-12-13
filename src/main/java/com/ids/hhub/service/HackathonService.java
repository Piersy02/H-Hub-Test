package com.ids.hhub.service;

import com.ids.hhub.dto.AddStaffDto;
import com.ids.hhub.dto.CreateHackathonDto;
import com.ids.hhub.model.*;
import com.ids.hhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HackathonService {

    //@Autowired private HackathonRepository hackathonRepo;
    private final HackathonRepository hackathonRepo;
    private final UserRepository userRepo;
    private final StaffAssignmentRepository staffRepo;

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

    public void addStaffMember(Long hackathonId, AddStaffDto dto, String requesterEmail) {
        // 1. Recupera l'utente che sta facendo la richiesta
        User requester = userRepo.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        // 2. CHECK DI SICUREZZA CONTESTUALE
        // "L'utente richiedente è ORGANIZER di QUESTO hackathon?"
        boolean isOrganizer = staffRepo.existsByUserIdAndHackathonIdAndRole(
                requester.getId(),
                hackathonId,
                StaffRole.ORGANIZER
        );

        // Se non è organizzatore E non è un super-admin globale, bloccalo.
        if (!isOrganizer && requester.getPlatformRole() != PlatformRole.ADMIN) {
            throw new SecurityException("ACCESSO NEGATO: Solo l'organizzatore può gestire lo staff.");
        }

        // 3. Logica di business (se il check passa)
        // ... aggiungi il nuovo staff ...
    }



    public List<Hackathon> getAllHackathons() {
        return hackathonRepo.findAll();
    }
}
