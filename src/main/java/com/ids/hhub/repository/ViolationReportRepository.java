package com.ids.hhub.repository;

import com.ids.hhub.model.ViolationReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViolationReportRepository extends JpaRepository<ViolationReport, Long> {}