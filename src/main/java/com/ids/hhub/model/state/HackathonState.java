package com.ids.hhub.model.state;

import com.ids.hhub.model.Hackathon;
import com.ids.hhub.model.Team;

public interface HackathonState {
    // Ogni stato deve decidere se accettare o rifiutare l'iscrizione di un team
    void registerTeam(Hackathon context, Team team);

    void submitProject(Hackathon context, Team team);

    void evaluateProject(Hackathon context);
}