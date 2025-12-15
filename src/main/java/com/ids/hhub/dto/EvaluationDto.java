package com.ids.hhub.dto;

import lombok.Data;

@Data
public class EvaluationDto {
    private int score;          // Es. 8
    private String comment;     // Es. "Ottimo codice, ma UI migliorabile"
}