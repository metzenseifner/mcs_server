package at.ac.uibk.mcsconnect.roomrepo.impl.hidden;

import at.ac.uibk.mcsconnect.common.api.CancellableRunnable;
import at.ac.uibk.mcsconnect.common.api.Preparable;
import at.ac.uibk.mcsconnect.executorservice.api.McsSingletonExecutorService;
import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.functional.common.Tuple;
import at.ac.uibk.mcsconnect.recorderservice.api.Metadata;
import at.ac.uibk.mcsconnect.recorderservice.api.Recorder;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderRunningStatesEnum;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstance;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import at.ac.uibk.mcsconnect.roomrepo.api.Terminal;
import at.ac.uibk.mcsconnect.sshsessionmanager.api.SshSessionManagerService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static at.ac.uibk.mcsconnect.common.api.DateTime.convertLocalDateTimeToDate;


/**
 * This concrete implementation of a {@link Room}.
 * <p>
 * This is an aggregate of several components:
 * - recorders
 * - terminals
 */
public class UibkRoom implements Room {

    private static final Logger LOGGER = LoggerFactory.getLogger(UibkRoom.class);

    private final String id;
    private final String name;
    private final Set<Recorder> recorders = new HashSet<>();
    private final Set<Terminal> terminals = new HashSet<>();

    //@Schema(description = "This is the aggregated state of many recorder running states.", accessMode = Schema.AccessMode.READ_ONLY)
    private volatile RecorderRunningStatesEnum polledRecordingRunningState = RecorderRunningStatesEnum.UNKNOWN;

    // TODO
    private Optional<RecordingInstance> recordingInstance;

    // TODO: rules mixed in with domain logic. consider extracting
    @JsonIgnore
    private Timer resetTimer;

    // Access to SshSessionManagerService
    @JsonIgnore
    private SshSessionManagerService sshSessionManagerService;

    /**
     * Function: {@link Recorder} -> {@link RecorderRunningStatesEnum}
     * Preparable expects result to be wrapped in type {@link Result}
     */
    @JsonIgnore
    private static final Preparable<Recorder, RecorderRunningStatesEnum> getRecorderRunningStatePreparable = recorder -> {
        try {
            return Result.success(recorder.getRecorderRunningState());
        } catch (Exception i) {
            return Result.failure(i);
        }
    };

    /**
     * Map that only returns when all of its keys have been updated from the time it is queried.
     */
    // States of all recorders are updated here
    //private final ConcurrentMap<Recorder, RecorderRunningStatesEnum> recorderRunningStatesMap = new ConcurrentHashMap<>();
    @JsonIgnore
    private final Map<CancellableRunnable, Future<?>> cancellableFutures = new HashMap<>();
    @JsonIgnore
    private static McsSingletonExecutorService taskService;
    @JsonIgnore
    private static ExecutorService executorService;
    @JsonIgnore
    private static ScheduledExecutorService schedexec;

    // Flag to determine whether this location has been synchronized successfully with its resources (i.e. recorders)
    private AtomicBoolean locationReady = new AtomicBoolean(false);

    public static UibkRoom create(String id, String name, Set<Recorder> recorders, Set<Terminal> terminals) {
        UibkRoom room = new UibkRoom(id, name, recorders, terminals);
        return room;
        //location.taskService = taskService;
        //executorService = taskService.getExecutorService();
        //schedexec = taskService.getScheduledExecutorService();
        //LOGGER.info(String.format("%s.create(%s, %s,...)", location.getClass().getSimpleName(), id, name));
        //return location;
    }

    /**
     * Constructor
     * <p>
     * OLD DOCUMENTATION BELOW
     * Create minimal location containing a recording instance.
     * <p>
     * Required 1:1 correspondence with a recording instance.
     */
    private UibkRoom(String id, String name, Set<Recorder> recorders, Set<Terminal> terminals) {
        this.id = id;
        this.name = name;
        this.recorders = recorders;
        this.terminals = terminals;
        this.recordingInstance = Optional.empty(); // DEFAULT
        // TODO This class should probably manage the threads for the recorders because outside code needs a way
        // TODO to cancel existing recorder threads
    }

    /**
     * Kickstarts the recorder synchronization threads.
     * <p>
     * Stores states of the recorders in {@link RecorderRunningStatesEnum}.
     */
    public void init() {

        // Create singleton task to determine whether this location is ready
        //CancellableRunnable taskDetermineLocationReady=new TaskDetermineLocationReady(this);
        //LOGGER.debug("Starting task to check whether this location is ready: \"{}\"", this);
        //try {
        //    ScheduledFuture<?> future = schedexec.scheduleAtFixedRate(
        //            taskDetermineLocationReady,
        //            5L, // init delay
        //            1L, // interval
        //            TimeUnit.SECONDS);
        //} catch (RejectedExecutionException e) {
        //    LOGGER.error("Submission of repetitively scheduled task rejected: {} {}", e.getMessage(), e.getCause());
        //}

        //CancellableRunnable taskAggregateRecordingRunningState = new TaskAggregateRecordingRunningState(this);
        //LOGGER.debug(String.format("%s.init() starting repetitively scheduled task to set polled recording running state", this));
        //try {
        //    ScheduledFuture<?> future = schedexec.scheduleAtFixedRate(
        //            taskAggregateRecordingRunningState,
        //            6L, // init delay
        //            1L, // interval
        //            TimeUnit.SECONDS);
        //} catch (RejectedExecutionException e) {
        //    LOGGER.error("Submission of repetitively scheduled task rejected: {} {}", e.getMessage(), e.getCause());
        //}
    }

    @Override
    public void destruct() {
        this.recorders.stream().forEach(r -> r.destruct());
    }

    @Override
    public String toString() {
        return String.format("UibkRoom(%s)", id);
    }

    @Override
    public synchronized String getId() {
        return id;
    }

    @Override
    public synchronized String getName() {
        return name;
    }

    ///**
    // * This is the entry point for a recording workflow. This method is
    // * executed by the old CamelEntityResolver, but now the new ____ TODO
    // * <p>
    // * We have to ensure:
    // * If user is the same, do not replace object to preserve the Bookings cache.
    // * If the recording instance is no longer valid, it must be replaced.
    // * - either time expired or user is different than owner
    // *
    // * @param loggedInUser
    // */
    //@Override
    //public synchronized void setUser(User loggedInUser) {
//
    //    // Map this.recordinginstance to a recording instance or empty.
    //    Result<RecordingInstance> recordingInstance = Result.of(this.recordingInstance)
    //            .map(Optional::get) // throws NoSuchElementException or value
    //            .filter(RecordingInstance::isWithinTimeThreshold)
    //            .filter(existing -> existing.getOwner().equals(loggedInUser));
//
    //    if (!recordingInstance.isSuccess()) {
    //        this.recordingInstance = Optional.empty();
    //    }
//
    //    // Map this.user to existing user or new user
    //    User userResult =
    //            Result.of(this.user).orElse(() -> Result.success(loggedInUser)) // TODO: Consider removing this.user null with Optional. See class member TODO.
    //                    .filter(old -> old.equals(loggedInUser))
    //                    .getOrElse(loggedInUser);
//
    //    this.user = userResult;
    //    // TODO: Consider implementing RecordingInstance.Builder such that the following will succeed:
    //    //.getOrElse(() -> new RecordingInstance.Builder()
    //    //        .with(b -> b.owner = loggedInUser).build()
    //    //);
    //    //LOGGER.info(String.format("%s.setUser(%s) detected a new user with a stale recording context. Resetting context.", this, loggedInUser));
    //    //LOGGER.info(String.format("%s.setUser(%s) detected the same user with a valid recording context.", this, loggedInUser));
    //}

    @Override
    public synchronized Set<Recorder> getRecorders() {
        return Collections.unmodifiableSet(recorders);
    }

    /**
     * Add set of recorders to this location. This is the init function
     * that starts the synchronization of recorder states.
     *
     * <ul>
     *     <li>This should be a blocking method until recorders states have been initialized to prevent
     *     calls to change running state before MCS is aware of the recorder states.</li>
     *     <li>Each should be given a reference to this owner class.</li>
     *     <li>Schedule a fetch state task at regular intervals. Tasks should fail if unsuccessful and
     *     the pr</li>
     * </ul>
     *
     * @param recorders
     */
    //@Override
    //public synchronized void setRecorders(Set<Recorder> recorders) {
//
    //    LOGGER.info("{} became aware of new recorders: ", this, recorders);
//
    //    this.recorders = recorders; // TODO: remove me and only use map.
    //    // TODO next lines cause major performance hit...
    //    //this.recorderRunningStatesMap = new PreparableAllOrNothingMap<>(recorders, fetchStatePreparable, () -> RecorderRunningStatesEnum.UNKNOWN, executorService);
    //    //this.recorderRunningStatesMap.prepare();
//
    //    recorders.stream().forEach(r -> {
    //        //    // a recorder is location-aware
    //        r.setRoom(this);
    //        //    // a recorder observes changes to the recording instance
    //        //    this.recordingInstance.get().attachObserver(r);
////
    //        //    // Init task to open channel to recorder (disabled because the SshChannelShellLockable.connect handles this now)
    //        //    // CancellableRunnable taskOpenChannel = new TaskOpenInitChannel(sshSessionManagerService, r);
////
    //        //    // Create task to fetch recorder running state
    //        //    // TODO: Reenable taskFetchRecorderState
    //        //    // CancellableRunnable taskFetchRecorderState = new TaskFetchRecorderRunningStateToMap();
    //    });
    //}

    /**
     * map recorders -> states
     * reduce states to 1 state (either they all match or UNKNOWN
     */
    @JsonIgnore
    public void aggregateRecordingRunningState() {
        try {

            // Populate map
            Map<Recorder, RecorderRunningStatesEnum> localRecorderRunningStatesMap = recorders.stream().collect(Collectors.toMap(r -> r, Recorder::getRecorderRunningState));

            LOGGER.debug(String.format("%s.aggregateRecordingRunningState() entries: %s", this, localRecorderRunningStatesMap.entrySet()));
            // Extract state
            Optional<RecorderRunningStatesEnum> reducedRecorderRunningState = localRecorderRunningStatesMap.values().stream()
                    .reduce((s1, s2) ->
                            s1.equals(s2)
                                    ? s1
                                    : RecorderRunningStatesEnum.UNKNOWN);

            if (reducedRecorderRunningState.isPresent()) { // test fails if 0 recorders
                this.setPolledRecordingRunningState(reducedRecorderRunningState.get());

                // TODO: UPDATE LOCATION READY STATE
                this.locationReady.set(reducedRecorderRunningState.equals(RecorderRunningStatesEnum.UNKNOWN)
                        ? false
                        : true);

            } else {
                this.locationReady.set(false);
            }

        } catch (Exception e) {
            LOGGER.error(String.format("%s.aggregateRecordingRunningState() error.", this), e);
        }
    }

    @Override
    public Set<Terminal> getTerminals() {
        return terminals;
    }

    //@Override
    //public Optional<Terminal> getTerminalById(String terminalId) {
    //    LOGGER.debug("{} looking for {}", this, terminalId);
    //    for (Terminal terminal : terminals) {
    //        if (terminal.getNetworkTarget().getHost().equals(terminalId)) {
    //            Optional<Terminal> optional = Optional.of(terminal);
    //            return optional;
    //        }
    //    }
    //    return Optional.empty();
    //}

    @Override
    public Optional<RecordingInstance> getRecordingInstance() {
        return this.recordingInstance;
    }

    /**
     * TODO This method does too much. It mixes effects that could be represented by
     * events e.g. Start an autostop thread for recording instance x, cancel an autostop thread for recording
     * instance x.
     *
     * Changes recording instance. Contains state transition logic for SMPs.
     * <p>
     * This contains critical logic for the system to work properly.
     * There are peculiarities to consider. If a client's
     *
     * @param newRecordingInstance
     */
    @Override
    public void setRecordingInstance(Optional<RecordingInstance> newRecordingInstance) {
        LOGGER.info(String.format("%s.setRecordingInstance(%s) called.", this, newRecordingInstance));

        Result<RecordingInstance> rOldRecordingInstance = getRecordingInstanceAsResult(this.recordingInstance);
        Result<RecordingInstance> rNewRecordingInstance = getRecordingInstanceAsResult(newRecordingInstance);

        this.recordingInstance = newRecordingInstance;

        if (newRecordingInstance.isPresent()) {
            if (newRecordingInstance.get().getRecordingRunningState().equals(RecorderRunningStatesEnum.STOPPED)) { // Stop explicitly requested, so stop whatever is recording
                notifyObserverOfRecordingRunningStateChange(RecorderRunningStatesEnum.STOPPED);
            }
        }

        Tuple<Result<RecordingInstance>, Result<RecordingInstance>> rRecordingInstanceTuple =
                new Tuple<>(rOldRecordingInstance, rNewRecordingInstance);

        //TODO: Replace embedded if logic with cleaner functional code

        RecordingInstance newR = rNewRecordingInstance.successValue(); // Anything else is an error //TODO make this cleaner
        if (rOldRecordingInstance.isSuccess()) {
            RecordingInstance oldR = rOldRecordingInstance.successValue();
            LOGGER.info(String.format("Old recording instance exists with state: %s", oldR.getRecordingRunningState()));

            // Exists Existing Recording Instance Case 1
            if (oldR.getRecordingRunningState().equals(newR.getRecordingRunningState())) {
                LOGGER.info(String.format("Old recording state is the same as the new recording state, so doing nothing. It is: %s", newR.getRecordingRunningState()));
                // DO NOTHING
            } else {

                // Exists Case 2: going from STOPPED to RECORDING
                if (oldR.getRecordingRunningState().equals(RecorderRunningStatesEnum.STOPPED)
                        && newR.getRecordingRunningState().equals(RecorderRunningStatesEnum.RECORDING)) {
                    notifyObserversOfMetadataChange(newR.getMetadata());
                    notifyObserverOfRecordingRunningStateChange(newR.getRecordingRunningState());
                } else {

                    // Exists Case 3: going from RECORDING to STOPPED
                    if (oldR.getRecordingRunningState().equals(RecorderRunningStatesEnum.RECORDING)
                            && newR.getRecordingRunningState().equals(RecorderRunningStatesEnum.STOPPED)) {
                        notifyObserverOfRecordingRunningStateChange(newR.getRecordingRunningState());
                    }
                }
            }
        } else { // Not Exists Existing Recording Instance
            LOGGER.info(String.format("Old recording instance does not exist."));
            if (newR.getRecordingRunningState().equals(RecorderRunningStatesEnum.RECORDING)) {
                notifyObserversOfMetadataChange(newR.getMetadata());
                notifyObserverOfRecordingRunningStateChange(newR.getRecordingRunningState());
            }
        }

        // TODO: This was put here to ensure that the old Timer gets disregarded but it should prob not be here because it is a side effect.
        this.resetTimer = new Timer();
        if (scheduleStopRecording(this, newR, resetTimer)) {
            LOGGER.info(String.format("%s.setRecordingInstance() scheduled stop at: %s", this, newR.getStopTime()));
        }

    }

    /**
     * This would be best triggered by an event.
     */
    private static boolean scheduleStopRecording(Room room,
                                                 RecordingInstance recordingInstance,
                                                 Timer resetTimer) {
        try {
            resetTimer.schedule(new TaskStopRecording(room, recordingInstance), convertLocalDateTimeToDate(recordingInstance.getStopTime()));
            return true;
        } catch (IllegalArgumentException a) {
            LOGGER.error(String.format("Could not schedule stop for %s", recordingInstance), a);
        } catch (IllegalStateException s) {
            LOGGER.error(String.format("Could not schedule stop for %s", recordingInstance), s);
        } catch (Exception e) {
            LOGGER.error(String.format("Could not schedule stop for %s", recordingInstance), e);
        }
        return false;
    }

    private void notifyObserversOfMetadataChange(Metadata metadata) {
        LOGGER.info("%s.notifyObserversOfMetadataChange(%s) called.", this, metadata);
        this.recorders.stream().forEach(r -> r.onMetadataChange(metadata));
    }
    //private void notifyObserversOfMetadataChange() {
    //    if (this.recordingInstance.isPresent()) {
    //        recordingInstance.get().notifyObservers(observer -> observer.onMetadataChange());
    //    }
    //}

    /**
     * Converted old observer pattern into push system to help with better with DAG dep graph TODO rename me
     */
    private void notifyObserverOfRecordingRunningStateChange(RecorderRunningStatesEnum recorderRunningStatesEnum) {
        LOGGER.info("%s.notifyObserverOfRecordingRunningStateChange(%s) called.", this, recorderRunningStatesEnum);
        this.recorders.stream().forEach(r -> r.onRecorderRunningStateChange(recorderRunningStatesEnum));
    }

    //private void notifyObserverOfRecordingRunningStateChange() {
    //    if (this.recordingInstance.isPresent()) {
    //        recordingInstance.get().notifyObservers(observer -> observer.onRecorderRunningStateChange(this.recordingInstance.get().getRecordingRunningState());
    //    }
    //}


    private static Result<RecordingInstance> getRecordingInstanceAsResult(Optional<RecordingInstance> recordingInstance) {
        try {
            return Result.success(recordingInstance.get());
        } catch (NoSuchElementException n) {
            return Result.failure("The optional recording instance must have been empty.");
        }
    }

    public void setPolledRecordingRunningState(RecorderRunningStatesEnum recordingRunningState) {
        this.polledRecordingRunningState = recordingRunningState;
    }

    public RecorderRunningStatesEnum getPolledRecordingRunningState() {
        return polledRecordingRunningState;
    }

    @Override
    public Boolean hasRecorders() {
        return this.recorders.size() == 0
                ? false
                : true;
    }

    @Override
    public Boolean hasTerminals() {
        return this.terminals.size() == 0
                ? false
                : true;
    }

    public AtomicBoolean isRoomReady() {
        return locationReady;
    }
}
