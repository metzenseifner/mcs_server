package at.ac.uibk.mcsconnect.roomrepo.api;

import at.ac.uibk.mcsconnect.bookingrepo.api.Booking;
import at.ac.uibk.mcsconnect.common.api.observerpattern.Observed;
import at.ac.uibk.mcsconnect.person.api.User;
import at.ac.uibk.mcsconnect.recorderservice.api.Metadata;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderRunningStatesEnum;
import at.ac.uibk.mcsconnect.recorderservice.api.RecordingInstanceObserver;

import java.time.LocalDateTime;

public abstract class RecordingInstance extends Observed<RecordingInstanceObserver> {

    public abstract Room getRoom();
    public abstract User getOwner();
    public abstract Booking getBooking();
    public abstract Metadata getMetadata();
    public abstract LocalDateTime getStopTime();
    public abstract String getRecordingName();
    public abstract RecorderRunningStatesEnum getRecordingRunningState(); // TODO prob not best place for this.

    private static final String TO_STRING_FORMATTER =
            "%s(room: %s owner: %s booking: %s stopTime: %s recordingName: %s recordingRunningState: %s)";

    @Override
    public String toString() {
        return String.format(TO_STRING_FORMATTER,
                this.getClass().getSimpleName(),
                getRoom(),
                getOwner(),
                getBooking(),
                getStopTime(),
                getRecordingName(),
                getRecordingRunningState());
    }
}
