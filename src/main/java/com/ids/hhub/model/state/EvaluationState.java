package com.ids.hhub.model.state;

import com.ids.hhub.model.Hackathon;
import com.ids.hhub.model.Team;

public class EvaluationState implements HackathonState {

    @Override
    public void registerTeam(Hackathon context, Team team) {
        // Nello stato di Valutazione, le iscrizioni sono chiuse
        throw new IllegalStateException("ERRORE: Le iscrizioni sono chiuse. I giudici stanno valutando i progetti.");
    }

    @Override
    public void submitProject(Hackathon context, Team team) {
        throw new IllegalStateException("Tempo scaduto! non è possibile sottoscrivere il progetto.");
    }

    @Override
    public void evaluateProject(Hackathon context) {
        // QUI NON FACCIO NULLA.
        // Il fatto che non lanci eccezione significa "Permesso Accordato".
    }

}