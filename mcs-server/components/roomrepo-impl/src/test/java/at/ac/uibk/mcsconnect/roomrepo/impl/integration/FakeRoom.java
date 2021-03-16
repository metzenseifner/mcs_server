package at.ac.uibk.mcsconnect.roomrepo.impl.integration;

import at.ac.uibk.mcsconnect.recorderservice.api.Recorder;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderRunningStatesEnum;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstance;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import at.ac.uibk.mcsconnect.roomrepo.api.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class FakeRoom implements Room {

    private static final Logger LOGGER = LoggerFactory.getLogger(FakeRoom.class);

    private String id;
    private String name;
    private Set<Recorder> recorders;
    private Set<Terminal> terminals;
    private Optional<RecordingInstance> recordingInstance;
    private RecorderRunningStatesEnum polledRecordingRunningState;

    public static Room create(String id, String name, Set<Recorder> recorders, Set<Terminal> terminals) {
        return new FakeRoom(id, name, recorders, terminals, Optional.empty());
    }

    public static Room create(String id, String name, Set<Recorder> recorders, Set<Terminal> terminals, Optional<RecordingInstance> recordingInstance) {
        return new FakeRoom(id, name, recorders, terminals, recordingInstance);
    }

    private FakeRoom(String id, String name, Set<Recorder> recorders, Set<Terminal> terminals, Optional<RecordingInstance> recordingInstance) {
        this.id = id;
        this.name = name;
        this.recorders = recorders;
        this.terminals = terminals;
        this.recordingInstance = recordingInstance;
        this.polledRecordingRunningState = RecorderRunningStatesEnum.UNKNOWN;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Set<Recorder> getRecorders() {
        return Collections.unmodifiableSet(this.recorders);
    }

    @Override
    public Set<Terminal> getTerminals() {
        return Collections.unmodifiableSet(this.terminals);
    }

    @Override
    public Boolean hasRecorders() {
        return recorders.size() > 0
                ? true
                : false;
    }

    @Override
    public Boolean hasTerminals() {
        return terminals.size() > 0
                ? true
                : false;
    }

    @Override
    public RecorderRunningStatesEnum getPolledRecordingRunningState() {
        return this.polledRecordingRunningState;
    }

    @Override
    public void setPolledRecordingRunningState(RecorderRunningStatesEnum recordingRunningState) {

    }

    @Override
    public Optional<RecordingInstance> getRecordingInstance() {
        return this.recordingInstance;
    }

    @Override
    public void setRecordingInstance(Optional<RecordingInstance> newRecordingInstance) {
        this.recordingInstance = newRecordingInstance;
    }

    @Override
    public String toString() {
        return String.format("FakeRoom(%s, %s)", this.id, this.name);
    }

    @Override
    public void destruct() {};
}
