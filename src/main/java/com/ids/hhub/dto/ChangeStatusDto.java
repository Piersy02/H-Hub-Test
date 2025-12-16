package com.ids.hhub.dto;

import com.ids.hhub.model.enums.HackathonStatus;
import lombok.Data;

@Data
public class ChangeStatusDto {
    private HackathonStatus newStatus; // Es. ONGOING, EVALUATION, FINISHED
}