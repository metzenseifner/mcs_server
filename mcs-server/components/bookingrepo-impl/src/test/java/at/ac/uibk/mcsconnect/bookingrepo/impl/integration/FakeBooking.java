package at.ac.uibk.mcsconnect.bookingrepo.impl.integration;

import at.ac.uibk.mcsconnect.bookingrepo.api.Booking;

import java.time.LocalDateTime;

public class FakeBooking implements Booking {

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

    public FakeBooking(
            long bookingId,
            LocalDateTime timeBegin,
            LocalDateTime timeEnd,
            long resourceId,
            String resourceName,
            String roomName,
            long courseId,
            String courseName,
            long courseNumber,
            String termId,
            long groupId,
            int groupNumber
    ) {
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
        return bookingId;
    }

    @Override
    public LocalDateTime getTimeBegin() {
        return timeBegin;
    }

    @Override
    public LocalDateTime getTimeEnd() {
        return timeEnd;
    }

    @Override
    public long getResourceId() {
        return resourceId;
    }

    @Override
    public String getResourceName() {
        return resourceName;
    }

    @Override
    public String getRoomName() {
        return roomName;
    }

    @Override
    public long getCourseId() {
        return courseId;
    }

    @Override
    public String getCourseName() {
        return courseName;
    }

    @Override
    public long getCourseNumber() {
        return courseNumber;
    }

    @Override
    public String getTermId() {
        return termId;
    }

    @Override
    public long getGroupId() {
        return groupId;
    }

    @Override
    public int getGroupNumber() {
        return groupNumber;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", this.getClass().getSimpleName(), this.bookingId);
    }
}
