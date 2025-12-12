package com.ids.hhub.dto;

import com.ids.hhub.model.StaffRole;
import lombok.Data;

@Data
public class AddStaffDto {
    private Long userId;       // Chi vuoi aggiungere?
    private StaffRole role;    // Che ruolo gli dai?
}
