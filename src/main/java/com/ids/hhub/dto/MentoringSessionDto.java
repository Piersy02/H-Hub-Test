package com.ids.hhub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class MentoringSessionDto {
    private Long teamId;        // Chi vuoi chiamare?
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String dateTime;    // Es. "2023-12-25 15:00" (Stringa per semplicità col Mock)
}