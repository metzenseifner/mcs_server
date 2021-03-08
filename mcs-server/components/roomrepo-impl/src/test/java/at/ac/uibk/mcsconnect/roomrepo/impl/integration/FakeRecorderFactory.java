package at.ac.uibk.mcsconnect.roomrepo.impl.integration;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
import at.ac.uibk.mcsconnect.executorservice.api.McsScheduledExecutorService;
import at.ac.uibk.mcsconnect.executorservice.api.McsSingletonExecutorService;
import at.ac.uibk.mcsconnect.recorderservice.api.Recorder;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderFactory;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderRunningStatesEnum;
import at.ac.uibk.mcsconnect.sshsessionmanager.api.SshSessionManagerService;

public class FakeRecorderFactory implements RecorderFactory  {
    @Override
    public Recorder create(String id, String name, NetworkTarget networkTargetUserPass, SshSessionManagerService sshSessionManager, McsScheduledExecutorService scheduledExecutorService, McsSingletonExecutorService singletonExecutorService) {
        return FakeRecorder.create(id, name, networkTargetUserPass, RecorderRunningStatesEnum.STOPPED);
    }
}
