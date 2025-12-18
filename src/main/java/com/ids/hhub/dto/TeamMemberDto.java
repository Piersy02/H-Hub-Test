package com.ids.hhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeamMemberDto {
    private String fullName;
    private String email;
}