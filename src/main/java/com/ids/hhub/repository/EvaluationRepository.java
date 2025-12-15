package com.ids.hhub.repository;

import com.ids.hhub.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    boolean existsBySubmissionAndJudge(Submission submission, User judge);
}