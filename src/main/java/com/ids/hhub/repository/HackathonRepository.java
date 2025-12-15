package com.ids.hhub.repository;

import com.ids.hhub.model.*;
import com.ids.hhub.model.enums.HackathonStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HackathonRepository extends JpaRepository<Hackathon, Long> {
    List<Hackathon> findByStatus(HackathonStatus status);
    Optional<Hackathon> findByName(String name);
}
