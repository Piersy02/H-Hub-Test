package com.ids.hhub.model.state;

import com.ids.hhub.model.Hackathon;
import com.ids.hhub.model.Team;

public class FinishedState implements HackathonState {
    @Override
    public void registerTeam(Hackathon context, Team team) {
        throw new IllegalStateException("ERRORE: Hackathon concluso. Non puoi iscriverti.");
    }

    @Override
    public void submitProject(Hackathon context, Team team) {
        throw new IllegalStateException("Tempo scaduto! non è possibile sottoscrivere il progetto.");
    }

    @Override
    public void evaluateProject(Hackathon context) {
        throw new IllegalStateException("ERRORE: Non è momento di votare!");
    }

}
