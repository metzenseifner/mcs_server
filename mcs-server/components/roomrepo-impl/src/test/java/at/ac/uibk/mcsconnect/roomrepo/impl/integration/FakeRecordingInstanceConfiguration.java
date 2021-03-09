package at.ac.uibk.mcsconnect.roomrepo.impl.integration;

import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstanceConfiguration;

import java.util.concurrent.TimeUnit;

public class FakeRecordingInstanceConfiguration implements RecordingInstanceConfiguration {

    private final long stopTimeExtensionThreshold;
    private final TimeUnit stopTimeExtensionThresholdUnit;

    public FakeRecordingInstanceConfiguration(long stopTimeExtensionThreshold, TimeUnit stopTimeExtensionThresholdUnit) {
        this.stopTimeExtensionThreshold = stopTimeExtensionThreshold;
        this.stopTimeExtensionThresholdUnit = stopTimeExtensionThresholdUnit;
    }

    @Override
    public long getStopTimeExtensionThreshold() {
        return stopTimeExtensionThreshold;
    }

    @Override
    public TimeUnit getStopTimeExtensionThresholdUnit() {
        return stopTimeExtensionThresholdUnit;
    }
}
