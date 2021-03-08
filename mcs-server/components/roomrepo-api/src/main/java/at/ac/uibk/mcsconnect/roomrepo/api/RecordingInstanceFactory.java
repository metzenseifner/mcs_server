package at.ac.uibk.mcsconnect.roomrepo.api;

import at.ac.uibk.mcsconnect.bookingrepo.api.Booking;
import at.ac.uibk.mcsconnect.person.api.User;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderRunningStatesEnum;

import java.time.LocalDateTime;

/**
 * TODO replace Room and User with string ids. Booking is still useful for the stop time and metadata.
 */
public interface RecordingInstanceFactory {

    RecordingInstance create(
            Room room,
            User user,
            Booking booking,
            //Set<Recorder> observers,
            LocalDateTime stopTime,
            String recordingName,
            RecorderRunningStatesEnum recorderRunningState
    );

}
