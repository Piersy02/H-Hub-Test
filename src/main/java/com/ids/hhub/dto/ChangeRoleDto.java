package com.ids.hhub.dto;

import com.ids.hhub.model.PlatformRole;
import lombok.Data;

@Data
public class ChangeRoleDto {
    private PlatformRole newRole; // Es: EVENT_CREATOR
}
