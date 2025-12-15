package com.ids.hhub.service.external;

public interface CalendarService {
    // Restituisce il link della call (es. meet.google.com/xyz)
    String scheduleMeeting(String mentorEmail, String teamLeaderEmail, String dateTime);
}