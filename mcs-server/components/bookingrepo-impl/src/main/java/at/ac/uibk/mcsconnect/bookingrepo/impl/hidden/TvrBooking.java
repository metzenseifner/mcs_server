package at.ac.uibk.mcsconnect.bookingrepo.impl.hidden;

public interface TvrBooking {
    String getExternalBookingId();
    String getDateFrom();
    String getDateUntil();
    String getResourceId();
    String getResourceName();
    String getLocationName();
    String getExternalCourseId();
    String getExternalGroupId();
    String getTermId();
    String getLvNr();
    String getLvTitle();
    String getGroupId();
}
