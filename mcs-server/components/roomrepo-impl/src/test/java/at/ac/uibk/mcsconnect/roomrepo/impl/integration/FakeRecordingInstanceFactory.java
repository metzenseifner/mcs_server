package at.ac.uibk.mcsconnect.roomrepo.impl.integration;

import at.ac.uibk.mcsconnect.bookingrepo.api.Booking;
import at.ac.uibk.mcsconnect.person.api.User;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderRunningStatesEnum;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstance;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstanceFactory;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;

import java.time.LocalDateTime;

public class FakeRecordingInstanceFactory implements RecordingInstanceFactory {
    @Override
    public RecordingInstance create(Room room, User user, Booking booking, LocalDateTime stopTime, String recordingName, RecorderRunningStatesEnum recorderRunningState) {
        return new FakeRecordingInstance(room, user, booking, stopTime, recordingName, recorderRunningState);
    }
}
