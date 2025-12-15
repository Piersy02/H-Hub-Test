package com.ids.hhub.model.state;

import com.ids.hhub.model.Hackathon;
import com.ids.hhub.model.Team;

public class OngoingState implements HackathonState {
    @Override
    public void registerTeam(Hackathon context, Team team) {
        throw new IllegalStateException("ERRORE: Le iscrizioni sono chiuse! L'Hackathon è già iniziato.");
    }
}