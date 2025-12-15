package com.ids.hhub.model.state;

import com.ids.hhub.model.Hackathon;
import com.ids.hhub.model.Team;

public class OngoingState implements HackathonState {
    @Override
    public void registerTeam(Hackathon context, Team team) {
        throw new IllegalStateException("ERRORE: Le iscrizioni sono chiuse! L'Hackathon è già iniziato.");
    }

    @Override
    public void submitProject(Hackathon context, Team team) {
        // Qui non facciamo nulla (o stampiamo un log),
        // il fatto che NON lanci eccezione significa "Permesso Accordato".
        System.out.println("Sottomissione accettata per il team " + team.getName());
    }

    @Override
    public void evaluateProject(Hackathon context) {
        throw new IllegalStateException("ERRORE: Non è momento di votare!");
    }

}