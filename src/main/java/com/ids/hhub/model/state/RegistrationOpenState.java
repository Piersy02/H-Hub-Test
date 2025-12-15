package com.ids.hhub.model.state;

import com.ids.hhub.model.Hackathon;
import com.ids.hhub.model.Team;

public class RegistrationOpenState implements HackathonState {
    @Override
    public void registerTeam(Hackathon context, Team team) {
        // Logica positiva: Aggiungi il team alla lista
        context.getTeams().add(team);
        System.out.println("Team " + team.getName() + " iscritto con successo!");
    }
}