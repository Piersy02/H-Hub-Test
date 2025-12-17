package com.ids.hhub.repository;

import com.ids.hhub.model.ViolationReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ViolationReportRepository extends JpaRepository<ViolationReport, Long> {
    // Trova tutte le segnalazioni di un determinato hackathon
    List<ViolationReport> findByHackathonId(Long hackathonId);
}
