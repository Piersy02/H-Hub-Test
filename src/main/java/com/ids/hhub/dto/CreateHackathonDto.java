package com.ids.hhub.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateHackathonDto {

    private String name;
    private String description;

    // Campi aggiuntivi richiesti dal progetto
    private String rules;                   // Regolamento
    private LocalDateTime registrationDeadline; // Scadenza iscrizioni
    private int maxTeamSize;                // Dimensione massima team

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private double prizeAmount;
}