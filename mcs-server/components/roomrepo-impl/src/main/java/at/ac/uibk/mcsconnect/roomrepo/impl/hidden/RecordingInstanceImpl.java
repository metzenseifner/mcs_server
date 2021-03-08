package at.ac.uibk.mcsconnect.roomrepo.impl.hidden;

import at.ac.uibk.mcsconnect.bookingrepo.api.Booking;
import at.ac.uibk.mcsconnect.common.api.Immutable;
import at.ac.uibk.mcsconnect.common.api.Views;
import at.ac.uibk.mcsconnect.person.api.User;
import at.ac.uibk.mcsconnect.recorderservice.api.Metadata;
import at.ac.uibk.mcsconnect.recorderservice.api.Recorder;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderRunningStatesEnum;
import at.ac.uibk.mcsconnect.roomrepo.api.MetadataUtils;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstance;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Represents a recording instance, which can
 * be a compilation of multiple videos from multiple
 * recorders. It defines the context of a recording.
 *
 * <h1>RecordingInstanceIntermediate Running State</h1>
 * The {@link this#recordingRunningState} is the canonical recording state at a {@link Room}.
 * It is actually an aggregated state of all of the recorders, whose value depends on the algorithm
 * that decides what the canonical state should be.
 * In order for it to be well-defined, every possible combination of states of recorders at a
 * {@link Room} must be mapped to this state.
 * The initial state is {@link RecorderRunningStatesEnum#UNKNOWN}.
 * Settable to {@link RecorderRunningStatesEnum#RECORDING} if at least one recorder is recording.
 * This is because it is safer to not abruptly stop a recording in progress and simply log an error.
 * If a recorder tries to import an unknown recording, it will likely be blocked, but that is ok because
 * capture agents must keep their files locally until they receive a successful import response.
 *
 * <p>
 * stopTime initialized endTime of Booking object, but is mutable for
 * option to extend time.
 * </p
 */
@Schema(name = "RecordingInstance",
        description = "Represents a recording's state at a given time." +
                        "The client may control some aspects of this class indirectly using an intermediate " +
                        "recording instance class.",
        accessMode = Schema.AccessMode.READ_ONLY
)
//@JsonDeserialize(builder = RecordingInstance.Builder.class)
@Immutable
public class RecordingInstanceImpl extends RecordingInstance { // extends Observed<RecordingInstanceObserver>

    // IMMUTABLE
    @JsonIgnore
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordingInstanceImpl.class);
    @JsonIgnore // prevent circular ref
    private final Room room; // TODO consider whether to just save roomId to make testing easier
    @JsonIgnore
    private final User owner;
    @JsonView(Views.Public.class)
    private final Booking booking;
    @JsonIgnore
    private final Metadata metadata;
    @JsonView(Views.Public.class)
    private final LocalDateTime stopTime;
    @JsonView(Views.Public.class)
    private final String recordingName;

    // THIS USED TO BE MUTABLE
    @JsonView(Views.Public.class)
    private final RecorderRunningStatesEnum recordingRunningState;

    /**
     * Design decision note: Decided that the caller, not the callee (originally Builder) should validate input. It
     * is easier to detect input errors when coding on the caller code side this way due to type check help.
     * On the other hand, the parameters here are a pain to change.
     *
     * @param location
     * @param user
     * @param booking
     * @param observers
     * @param stopTime
     * @param recordingName
     * @param recordingRunningState
     * @return
     */
    public static RecordingInstanceImpl apply(Room location, User user, Booking booking, Set<Recorder> observers, LocalDateTime stopTime, String recordingName, RecorderRunningStatesEnum recordingRunningState) {
        return new RecordingInstanceImpl(location, user, booking, observers, stopTime, recordingName, recordingRunningState);
    }

    /**
     * Private Constructor
     */
    private RecordingInstanceImpl(
            Room room,
            User user,
            Booking booking,
            Set<Recorder> observers,
            LocalDateTime stopTime,
            String recordingName,
            RecorderRunningStatesEnum recordingRunningState) {
        super();
        this.room = room;
        observers.stream().forEach(this::attachObserver); //TODO bind concrete class to observer set
        this.owner = user;
        this.booking = booking;
        this.stopTime = stopTime;
        this.recordingName = recordingName;
        this.metadata = MetadataUtils.generateMetadataDataset(user, booking, recordingName); // TODO: Gray area: does this leak SMP-specific stuff into this class or is DublinCore really part of this?
        this.recordingRunningState = recordingRunningState;
        //scheduleNewResetTask();
    }



    public User getOwner() {
        return owner;
    }

    public String getRecordingName() {
        return recordingName;
    }

    /**
     * Get the booking associated with {@link this}.
     *
     * @return Booking (or null if no booking context set)
     */
    public Booking getBooking() {
        return booking;
    }

    public LocalDateTime getStopTime() {
        return stopTime;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public RecorderRunningStatesEnum getRecordingRunningState() {
        return this.recordingRunningState;
    }

    public Room getRoom() {
        return room;
    }

}
