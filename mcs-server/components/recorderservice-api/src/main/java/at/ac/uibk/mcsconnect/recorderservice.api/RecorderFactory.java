package at.ac.uibk.mcsconnect.recorderservice.api;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
import at.ac.uibk.mcsconnect.executorservice.api.McsScheduledExecutorService;
import at.ac.uibk.mcsconnect.executorservice.api.McsSingletonExecutorService;
import at.ac.uibk.mcsconnect.sshsessionmanager.api.SshSessionManagerService;

/**
 * It is very important to call {@link Recorder#init()}  after
 * creating a new {@link Recorder} object. This necessary to activate
 * any other start-up things that may only be activated after the object
 * is fully constructed e.g. passing this reference to a thread.
 */
public interface RecorderFactory {
    Recorder create(String id,
                    String name,
                    NetworkTarget networkTargetUserPass,
                    SshSessionManagerService sshSessionManager,
                    McsScheduledExecutorService scheduledExecutorService,
                    McsSingletonExecutorService singletonExecutorService
    );
}
