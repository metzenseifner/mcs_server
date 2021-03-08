package at.ac.uibk.mcsconnect.roomrepo.impl.integration;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.recorderservice.api.Metadata;
import at.ac.uibk.mcsconnect.recorderservice.api.Recorder;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderRunningStatesEnum;
import at.ac.uibk.mcsconnect.recorderservice.api.SisProtocol;

public class FakeRecorder implements Recorder {

    private String id;
    private String name;
    private NetworkTarget networkTarget;
    private RecorderRunningStatesEnum recorderRunningState;

    public static Recorder create(
            String id,
            String name,
            NetworkTarget networkTarget,
            RecorderRunningStatesEnum initialState
    ) {
        return new FakeRecorder(id, name, networkTarget, initialState);
    }

    private FakeRecorder(
            String id,
            String name,
            NetworkTarget networkTarget,
            RecorderRunningStatesEnum initialState
    ) {
        this.id = id;
        this.name = name;
        this.networkTarget = networkTarget;
        this.recorderRunningState = initialState;
    }

    @Override
    public void onMetadataChange(Metadata metadata) {

    }

    @Override
    public void onRecorderRunningStateChange(RecorderRunningStatesEnum recorderRunningStatesEnum) {

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
    public NetworkTarget getNetworkTarget() {
        return this.networkTarget;
    }

    @Override
    public RecorderRunningStatesEnum getRecorderRunningState() {
        return this.recorderRunningState;
    }

    @Override
    public void setRecorderRunningState(RecorderRunningStatesEnum recorderRunningState) {
        this.recorderRunningState = recorderRunningState;
    }

    @Override
    public Result<String> sendMessage(SisProtocol.Gettable recorderCommandsEnum) {
        return Result.empty();
    }

    @Override
    public void sendMessage(SisProtocol.Settable recorderCommandsEnum, String value) {

    }

    @Override
    public void sendMessage(SisProtocol.Command recorderCommandEnum) {

    }

    @Override
    public String toString() {
        return String.format("%s(%s)", this.getClass().getSimpleName(), getId());
    }
}
