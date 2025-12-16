package com.ids.hhub.repository;

import com.ids.hhub.model.SupportRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {
    List<SupportRequest> findByHackathonId(Long hackathonId);
}
