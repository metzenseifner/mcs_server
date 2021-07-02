package at.ac.uibk.mcsconnect.recorderservice.impl;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
import at.ac.uibk.mcsconnect.executorservice.api.McsScheduledExecutorService;
import at.ac.uibk.mcsconnect.executorservice.api.McsSingletonExecutorService;
import at.ac.uibk.mcsconnect.recorderservice.api.Recorder;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderFactory;
import at.ac.uibk.mcsconnect.recorderservice.impl.hidden.Smp351;
import at.ac.uibk.mcsconnect.sshsessionmanager.api.SshSessionManagerService;
import org.osgi.service.component.annotations.Component;

@Component(
        name = "at.ac.uibk.mcsconnect.recorderservice.impl.RecorderFactoryImpl"
)
public class RecorderFactoryImpl implements RecorderFactory {
    @Override
    public Recorder create(String id,
                           String name,
                           NetworkTarget networkTargetUserPass,
                           SshSessionManagerService sshSessionManager,
                           McsScheduledExecutorService scheduledExecutorService,
                           McsSingletonExecutorService singletonExecutorService) {
        Recorder recorder = Smp351.create(id, name, networkTargetUserPass, sshSessionManager, scheduledExecutorService, singletonExecutorService);
        recorder.init();
        return recorder;
    }
}
