package com.ids.hhub.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateHackathonDto {
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private double prizeAmount;
    private Long organizerId; // ID dell'utente che crea
}