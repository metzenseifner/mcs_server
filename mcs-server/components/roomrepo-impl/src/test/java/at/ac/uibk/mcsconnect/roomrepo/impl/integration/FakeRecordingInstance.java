package at.ac.uibk.mcsconnect.roomrepo.impl.integration;

import at.ac.uibk.mcsconnect.bookingrepo.api.Booking;
import at.ac.uibk.mcsconnect.person.api.User;
import at.ac.uibk.mcsconnect.recorderservice.api.Metadata;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderRunningStatesEnum;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstance;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import at.ac.uibk.mcsconnect.roomrepo.api.MetadataUtils;

import java.time.LocalDateTime;

public class FakeRecordingInstance extends RecordingInstance {

    private final Room room; // TODO consider whether to just save roomId to make testing easier
    private final User owner;
    private final Booking booking;
    private final Metadata metadata;
    private final LocalDateTime stopTime;
    private final String recordingName;
    private final RecorderRunningStatesEnum recordingRunningState;

    public FakeRecordingInstance(Room room, User user, Booking booking, LocalDateTime stopTime, String recordingName, RecorderRunningStatesEnum recordingRunningState) {
        this.room = room;
        this.owner = user;
        this.booking = booking;
        this.metadata = MetadataUtils.generateMetadataDataset(user, booking, recordingName);
        this.stopTime = stopTime;
        this.recordingName = recordingName;
        this.recordingRunningState = recordingRunningState;
    }

    @Override
    public Room getRoom() {
        return room;
    }

    @Override
    public User getOwner() {
        return owner;
    }

    @Override
    public Booking getBooking() {
        return booking;
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public LocalDateTime getStopTime() {
        return stopTime;
    }

    @Override
    public String getRecordingName() {
        return recordingName;
    }

    @Override
    public RecorderRunningStatesEnum getRecordingRunningState() {
        return this.recordingRunningState;
    }
}
