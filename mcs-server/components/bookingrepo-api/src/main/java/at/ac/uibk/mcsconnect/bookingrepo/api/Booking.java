package at.ac.uibk.mcsconnect.bookingrepo.api;

import java.time.LocalDateTime;

/**
 * This represents a booking at the university.
 *
 * It uses semantic naming for each property.
 */
public interface Booking {
    long getBookingId();
    LocalDateTime getTimeBegin();
    LocalDateTime getTimeEnd();
    long getResourceId();
    String getResourceName();
    String getRoomName();
    long getCourseId();
    String getCourseName();
    long getCourseNumber();
    String getTermId();
    long getGroupId();
    int getGroupNumber();
}
