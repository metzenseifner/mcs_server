package at.ac.uibk.mcsconnect.recorderservice.api;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
import at.ac.uibk.mcsconnect.executorservice.api.McsScheduledExecutorService;
import at.ac.uibk.mcsconnect.executorservice.api.McsSingletonExecutorService;
import at.ac.uibk.mcsconnect.sshsessionmanager.api.SshSessionManagerService;

public interface RecorderFactory {
    Recorder create(String id,
                    String name,
                    NetworkTarget networkTargetUserPass,
                    SshSessionManagerService sshSessionManager,
                    McsScheduledExecutorService scheduledExecutorService,
                    McsSingletonExecutorService singletonExecutorService
    );
}
