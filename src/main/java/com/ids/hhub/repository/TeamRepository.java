package com.ids.hhub.repository;

import com.ids.hhub.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByNameAndHackathonId(String name, Long hackathonId);
    Optional<Team> findByName(String name);
}
