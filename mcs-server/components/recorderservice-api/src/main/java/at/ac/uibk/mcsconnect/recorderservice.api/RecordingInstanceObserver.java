package at.ac.uibk.mcsconnect.recorderservice.api;

import at.ac.uibk.mcsconnect.recorderservice.api.Metadata;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderRunningStatesEnum;

/**
 * Contains notification methods that can be triggered
 * by recording instances.
 */
public interface RecordingInstanceObserver {

    void onMetadataChange(Metadata metadata);
    void onRecorderRunningStateChange(RecorderRunningStatesEnum recorderRunningStatesEnum);

}
