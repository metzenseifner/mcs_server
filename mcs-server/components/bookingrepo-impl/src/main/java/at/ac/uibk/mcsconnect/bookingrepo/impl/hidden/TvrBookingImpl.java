package at.ac.uibk.mcsconnect.bookingrepo.impl.hidden;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

// @Immutable TODO wanted immutalble by XML mapper fails.
@JacksonXmlRootElement(localName = "booking")
public class TvrBookingImpl implements TvrBooking {
    private String externalBookingId;
    private String dateFrom;
    private String dateUntil;
    private String resourceId;
    private String resourceName;
    private String locationName;
    private String externalCourseId;
    private String externalGroupId;
    private String termId;
    private String lvNr;
    private String lvTitle;
    private String groupId;

    //@JsonCreator(mode = JsonCreator.Mode.PROPERTIES) // TODO XML mappers seems to ignore this but should not
    //public static final Booking apply(
    //                                  @JsonProperty("externalBookingId") String bookingId,
    //                                  @JsonProperty("dateFrom") String timeBegin,
    //                                  @JsonProperty("dateUntil") String timeEnd,
    //                                  @JsonProperty("resourceId") String resourceId,
    //                                  @JsonProperty("resourceName") String resourceName,
    //                                  @JsonProperty("locationName") String locationName,
    //                                  @JsonProperty("externalCourseId") String courseId,
    //                                  @JsonProperty("externalGroupId") String groupId,
    //                                  @JsonProperty("termId") String termId,
    //                                  @JsonProperty("lvNr") String courseNumber,
    //                                  @JsonProperty("lvTitle") String courseName,
    //                                  @JsonProperty("groupId") String groupIndex) {
    //    return new Booking(
    //            bookingId,
    //            timeBegin,
    //            timeEnd,
    //            resourceId,
    //            resourceName,
    //            locationName,
    //            courseId,
    //            groupId,
    //            termId,
    //            courseNumber,
    //            courseName,
    //            groupIndex);
    //}
//
    ////@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    //private Booking( // TODO made public only because of xml issue with static factory
    //        String bookingId,
    //        String timeBegin,
    //        String timeEnd,
    //        String resourceId,
    //        String resourceName,
    //        String locationName,
    //        String courseId,
    //        String groupId,
    //        String termId,
    //                 String courseNumber,
    //        String courseName,
    //        String groupIndex) {
    //    this.bookingId = bookingId;
    //    this.timeBegin = timeBegin;
    //    this.timeEnd = timeEnd;
    //    this.resourceId = resourceId;
    //    this.resourceName = resourceName;
    //    this.locationName = locationName;
    //    this.courseId = courseId;
    //    this.groupId = groupId;
    //    this.termId = termId;
    //    this.courseNumber = courseNumber;
    //    this.courseName = courseName;
    //    this.groupIndex = groupIndex;
    //}

    // TODO remove this when making immutable. XML mapper was ignoring @JsonCreator; forced to add default constructor.
    public TvrBookingImpl() {}


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
                "bookingId", externalBookingId,
                "timeBegin", dateFrom,
                "timeEnd", dateUntil,
                "resourceId", resourceId,
                "resourceName", resourceName,
                "locationName", locationName,
                "courseId", externalCourseId,
                "groupId", externalGroupId,
                "termId", termId,
                "courseNumber", lvNr,
                "courseName", lvTitle,
                "groupIndex", groupId);
    }

    public String getExternalBookingId() {
        return externalBookingId;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public String getDateUntil() {
        return dateUntil;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getExternalCourseId() {
        return externalCourseId;
    }

    public String getExternalGroupId() {
        return externalGroupId;
    }

    public String getTermId() {
        return termId;
    }

    public String getLvNr() {
        return lvNr;
    }

    public String getLvTitle() {
        return lvTitle;
    }

    public String getGroupId() {
        return groupId;
    }

    // SETTER

    public void setExternalBookingId(String externalBookingId) {
        this.externalBookingId = externalBookingId;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public void setDateUntil(String dateUntil) {
        this.dateUntil = dateUntil;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public void setExternalCourseId(String externalCourseId) {
        this.externalCourseId = externalCourseId;
    }

    public void setExternalGroupId(String externalGroupId) {
        this.externalGroupId = externalGroupId;
    }

    public void setTermId(String termId) {
        this.termId = termId;
    }

    public void setLvNr(String lvNr) {
        this.lvNr = lvNr;
    }

    public void setLvTitle(String lvTitle) {
        this.lvTitle = lvTitle;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
