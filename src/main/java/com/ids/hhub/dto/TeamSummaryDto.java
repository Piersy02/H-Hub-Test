package com.ids.hhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class TeamSummaryDto {
    private Long teamId;
    private String teamName;
    private String leaderEmail;
    private String leaderName;
    private List<TeamMemberDto> members;
}