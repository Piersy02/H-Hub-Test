package com.ids.hhub.dto;

import com.ids.hhub.model.enums.PlatformRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSummaryDto {
    private Long id;
    private String fullName;
    private String email;
    private PlatformRole role;
    private String teamName;    // Nome del team (o "N/A" se null)
    private boolean isTeamLeader;   // true se è il leader
}