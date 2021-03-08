package at.ac.uibk.mcsconnect.roomrepo.api;

import at.ac.uibk.mcsconnect.common.api.Views;
import at.ac.uibk.mcsconnect.recorderservice.api.Recorder;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderRunningStatesEnum;
import com.fasterxml.jackson.annotation.JsonView;

import java.util.Optional;
import java.util.Set;

public interface Room {

    /**
     * Get the machine-readable room id.
     *
     * @return id of location
     */
    @JsonView(Views.Public.class)
    String getId();

    /**
     * Get human-readable name of room.
     *
     * @return name as String.
     */
    @JsonView(Views.Public.class)
    String getName();

    /**
     * Get all {@link Recorder} objects at this location.
     *
     * @return a Operation of recorders
     */
    //@JsonView(Views.Admin.class)
    //@JsonManagedReference
    Set<Recorder> getRecorders();

    /**
     * Get all {@link Terminal} objects at this location.
     *
     * @return
     */
    //@JsonView(Views.Public.class)
    //@JsonBackReference
    Set<Terminal> getTerminals();

    /**
     * Determine whether a location has recording devices.
     *
     * @return Boolean
     */
    //@JsonView(Views.Admin.class)
    Boolean hasRecorders();

    /**
     * Determine whether this {@link Room} has a {@link Terminal} (normally a singleton).
     * @return
     */
    Boolean hasTerminals();

    /**
     * Access live polled running state.
     * @return
     */
    //@JsonView(Views.Public.class)
    RecorderRunningStatesEnum getPolledRecordingRunningState();

    /**
     * Allow live running state to be set. This operation must be atomic and threadsafe.
     */
    void setPolledRecordingRunningState(RecorderRunningStatesEnum recordingRunningState);


    /**
     * Determines whether location has been synchronized with its resources.
     *
     * @return
     */
    //@JsonView(Views.Admin.class)
    //AtomicBoolean isRoomReady();

    /**
     * This is the awkward member. It represents a singleton
     * object that can either exist (when a user is logged in)
     * or not exist at a {@link Room}.
     *
     * @return
     */
    Optional<RecordingInstance> getRecordingInstance();

    /**
     * This is the akward mutable state of {@link Room}.
     *
     * @param newRecordingInstance
     */
    void setRecordingInstance(Optional<RecordingInstance> newRecordingInstance);

}
