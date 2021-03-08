package at.ac.uibk.mcsconnect.roomrepo.api;

import java.util.concurrent.TimeUnit;

public interface RecordingInstanceConfiguration {

    long getStopTimeExtensionThreshold();
    TimeUnit getStopTimeExtensionThresholdUnit();
}
