package com.ids.hhub.model.state;

import com.ids.hhub.model.Hackathon;
import com.ids.hhub.model.Team;

public class EvaluationState implements HackathonState {

    @Override
    public void registerTeam(Hackathon context, Team team) {
        // Nello stato di Valutazione, le iscrizioni sono chiuse
        throw new IllegalStateException("ERRORE: Le iscrizioni sono chiuse. I giudici stanno valutando i progetti.");
    }

    // NOTA PER IL FUTURO (Iterazione 3):
    // Qui aggiungeremo un metodo tipo:
    // @Override
    // public void submitEvaluation(...) {
    //     // Qui sarà permesso!
    // }
}