package com.ids.hhub.dto;

import lombok.Data;

@Data
public class MentoringSessionDto {
    private Long teamId;        // Chi vuoi chiamare?
    private String dateTime;    // Es. "2023-12-25 15:00" (Stringa per semplicità col Mock)
}