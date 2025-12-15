package com.ids.hhub.service.external;

import org.springframework.stereotype.Service;
import java.util.UUID;

@Service("mockCalendar") // Nome del bean
public class MockCalendarService implements CalendarService {

    @Override
    public String scheduleMeeting(String mentorEmail, String teamLeaderEmail, String dateTime) {
        System.out.println("[MOCK CALENDAR] Prenotazione da " + mentorEmail + " per " + teamLeaderEmail + " alle " + dateTime);
        // Genera un link finto
        return "https://meet.fake-calendar.com/" + UUID.randomUUID().toString();
    }
}