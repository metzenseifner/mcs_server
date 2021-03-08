package at.ac.uibk.mcsconnect.http.impl.hidden.datatransferobject;

import at.ac.uibk.mcsconnect.common.api.Immutable;
import at.ac.uibk.mcsconnect.common.api.Views;
import at.ac.uibk.mcsconnect.http.api.DTO;
import com.fasterxml.jackson.annotation.JsonView;

import java.time.LocalDateTime;

@Immutable
public class BookingDTO implements DTO {

    @JsonView(Views.Public.class)
    public final long bookingId;
    @JsonView(Views.Public.class)
    public final LocalDateTime timeBegin;
    @JsonView(Views.Public.class)
    public final LocalDateTime timeEnd;
    @JsonView(Views.Public.class)
    public final long resourceId;
    @JsonView(Views.Public.class)
    public final String locationName;
    @JsonView(Views.Public.class)
    public final long courseId;
    @JsonView(Views.Public.class)
    public final long groupId;
    @JsonView(Views.Public.class)
    public final String termId;
    @JsonView(Views.Public.class)
    public final long courseNumber;
    @JsonView(Views.Public.class)
    public final String courseName;
    @JsonView(Views.Public.class)
    public final int groupNumber;

    public BookingDTO(long bookingId, LocalDateTime timeBegin, LocalDateTime timeEnd, long resourceId, String locationName, long courseId, long groupId, String termId, long courseNumber, String courseName, int groupNumber) {
        this.bookingId = bookingId;
        this.timeBegin = timeBegin;
        this.timeEnd = timeEnd;
        this.resourceId = resourceId;
        this.locationName = locationName;
        this.courseId = courseId;
        this.groupId = groupId;
        this.termId = termId;
        this.courseNumber = courseNumber;
        this.courseName = courseName;
        this.groupNumber = groupNumber;
    }
}