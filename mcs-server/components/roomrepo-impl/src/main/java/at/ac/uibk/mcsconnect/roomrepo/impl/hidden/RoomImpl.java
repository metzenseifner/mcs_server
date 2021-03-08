//package at.ac.uibk.mcsconnect.roomservice.impl.hidden;
//
//import at.ac.uibk.mcsconnect.person.api.User;
//import at.ac.uibk.mcsconnect.recorderservice.api.Recorder;
//import at.ac.uibk.mcsconnect.recorderservice.api.RecorderRunningStatesEnum;
//import RecordingInstance;
//import Room;
//import Terminal;
//import at.ac.uibk.mcsconnect.sshsessionmanager.api.SshSessionManagerService;
//import com.fasterxml.jackson.annotation.JsonIgnore;
//
//import java.util.HashSet;
//import java.util.Optional;
//import java.util.Set;
//import java.util.Timer;
//import java.util.concurrent.atomic.AtomicBoolean;
//
///**
// * This new implementation of room recovers from errors made in the
// * initial design. Rooms are immutable objects that are defined
// * independently of the {@link RecordingInstance}s.
// */
//public class RoomImpl implements Room {
//
//    private String id;
//    private String name;
//    private Set<Recorder> recorders = new HashSet<>();
//    private Set<Terminal> terminals = new HashSet<>();
//
//    // TODO: Consider this decision to make a recording instance a part of this aggregate object
//    private Optional<RecordingInstance> recordingInstance;
//
//    private volatile RecorderRunningStatesEnum polledRecordingRunningState = RecorderRunningStatesEnum.UNKNOWN;
//
//    @JsonIgnore
//    private Timer resetTimer;
//
//    // Access to SshSessionManagerService
//    @JsonIgnore
//    private SshSessionManagerService sshSessionManagerService;
//
//    public RoomImpl(String id, String name, Set<Recorder> recorders, Set<Terminal> terminals) {
//        this.id = id;
//        this.name = name;
//        this.recorders = recorders;
//        this.terminals = terminals;
//    }
//
//    @Override
//    public String getId() {
//        return null;
//    }
//
//    @Override
//    public String getName() {
//        return null;
//    }
//
//    @Override
//    public Set<Recorder> getRecorders() {
//        return null;
//    }
//
//    @Override
//    public Set<Terminal> getTerminals() {
//        return null;
//    }
//
//    @Override
//    public Boolean hasRecorders() {
//        return null;
//    }
//
//    @Override
//    public Boolean hasTerminals() {
//        return null;
//    }
//
//    @Override
//    public RecorderRunningStatesEnum getPolledRecordingRunningState() {
//        return null;
//    }
//
//    @Override
//    public void setPolledRecordingRunningState(RecorderRunningStatesEnum recordingRunningState) {
//
//    }
//
//    @Override
//    public AtomicBoolean isRoomReady() {
//        return null;
//    }
//}
