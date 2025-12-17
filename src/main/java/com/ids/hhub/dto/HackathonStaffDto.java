package com.ids.hhub.dto;

import com.ids.hhub.model.StaffAssignment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class HackathonStaffDto extends HackathonPublicDto {

    // Dati sensibili visibili solo allo staff
    private double prizeAmount; // Il budget

    // Lista completa dello staff con dettagli
    private List<StaffAssignment> fullStaffList;

    // Eventuali note interne o statistiche
    private int totalTeamsRegistered;
}
