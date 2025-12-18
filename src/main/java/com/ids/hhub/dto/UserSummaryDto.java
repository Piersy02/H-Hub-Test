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
}