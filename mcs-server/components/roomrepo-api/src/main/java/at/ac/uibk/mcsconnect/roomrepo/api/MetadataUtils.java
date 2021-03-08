package at.ac.uibk.mcsconnect.roomrepo.api;

import at.ac.uibk.mcsconnect.bookingrepo.api.Booking;
import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.person.api.User;
import at.ac.uibk.mcsconnect.recorderservice.api.DublinCore;
import at.ac.uibk.mcsconnect.recorderservice.api.Metadata;

public final class MetadataUtils {

    /**
     * Defines mapping between {@link DublinCore} and the {@link RecordingInstance}.
     * @param owner
     * @param booking
     * @param recordingName
     * @return
     */
    public final static Metadata generateMetadataDataset(User owner, Booking booking, String recordingName) {
        return new Metadata.Builder().with(b -> {
            b.withProperty(DublinCore.CREATOR, Result.of(owner.getUserId()).map(String::valueOf).getOrElse(""));
            b.withProperty(DublinCore.RELATION, Result.of(booking.getBookingId()).map(String::valueOf).getOrElse(""));
            b.withProperty(DublinCore.SOURCE, Result.of(booking.getGroupId()).map(String::valueOf).getOrElse(""));
            b.withProperty(DublinCore.SUBJECT, Result.of(String.format("%s-%s %s %s", booking.getCourseNumber(), booking.getGroupNumber(), booking.getTermId(), booking.getCourseName())).getOrElse(""));
            b.withProperty(DublinCore.TITLE, Result.of(recordingName).getOrElse(""));
        }).build();
    }

}
