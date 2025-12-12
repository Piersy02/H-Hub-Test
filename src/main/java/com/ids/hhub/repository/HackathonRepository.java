package com.ids.hhub.repository;

import com.ids.hhub.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HackathonRepository extends JpaRepository<Hackathon, Long> {
    List<Hackathon> findByState(HackathonState state);
}
