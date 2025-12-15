package com.ids.hhub.dto;

import lombok.Data;

@Data
public class SubmissionDto {
    private String projectUrl;  // Es. "https://github.com/my-repo"
    private String description; // Es. "App per la gestione rifiuti"
}