package com.ids.hhub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HackathonPublicDto {
    private Long id;
    private String name;
    private String description;
    private String location;
    private String status; // Stringa dell'Enum

    // Date importanti per il pubblico
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationDeadline;

    // Info generiche
    private int maxTeamSize;
    private String winnerTeamName; // Solo il nome, se c'è
}