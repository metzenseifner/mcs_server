package at.ac.uibk.mcsconnect.bookingrepo.impl;

import at.ac.uibk.mcsconnect.bookingrepo.api.Booking;

import java.time.LocalDateTime;

public class BookingImpl implements Booking {

    private final long bookingId;
    private final LocalDateTime timeBegin;
    private final LocalDateTime timeEnd;
    private final long resourceId;
    private final String resourceName;
    private final String roomName;
    private final long courseId;
    private final String courseName;
    private final long courseNumber;
    private final String termId;
    private final long groupId;
    private final int groupNumber;

    public BookingImpl(long bookingId, LocalDateTime timeBegin, LocalDateTime timeEnd, long resourceId, String resourceName, String roomName, long courseId, String courseName, long courseNumber, String termId, long groupId, int groupNumber) {
        this.bookingId = bookingId;
        this.timeBegin = timeBegin;
        this.timeEnd = timeEnd;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.roomName = roomName;
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseNumber = courseNumber;
        this.termId = termId;
        this.groupId = groupId;
        this.groupNumber = groupNumber;
    }

    @Override
    public long getBookingId() {
        return this.bookingId;
    }

    @Override
    public LocalDateTime getTimeBegin() {
        return this.timeBegin;
    }

    @Override
    public LocalDateTime getTimeEnd() {
        return this.timeEnd;
    }

    @Override
    public long getResourceId() {
        return this.resourceId;
    }

    @Override
    public String getResourceName() {
        return this.resourceName;
    }

    public String getRoomName() {
        return this.roomName;
    }

    @Override
    public long getCourseId() {
        return this.courseId;
    }

    @Override
    public String getCourseName() {
        return this.courseName;
    }

    @Override
    public long getCourseNumber() {
        return this.courseNumber;
    }

    @Override
    public String getTermId() {
        return this.termId;
    }

    @Override
    public long getGroupId() {
        return this.groupId;
    }

    @Override
    public int getGroupNumber() {
        return this.groupNumber;
    }


    private final static String FORMATTER = "%s(" +
            "%s: %s, " +
            "%s: %s, " +
            "%s: %s, " +
            "%s: %s, " +
            "%s: %s, " +
            "%s: %s, " +
            "%s: %s, " +
            "%s: %s, " +
            "%s: %s, " +
            "%s: %s, " +
            "%s: %s, " +
            "%s: %s)";

    @Override
    public String toString() {
        return String.format(FORMATTER,
                getClass().getSimpleName(),
                "bookingId", this.bookingId,
                "timeBegin", this.timeBegin,
                "timeEnd", this.timeEnd,
                "resourceId", this.resourceId,
                "resourceName", this.resourceName,
                "roomName", this.roomName,
                "termId", this.termId,
                "courseId", this.courseId,
                "courseNumber", this.courseNumber,
                "courseName", this.courseName,
                "groupId", this.groupId,
                "groupNumber", this.groupNumber);
    }
}
