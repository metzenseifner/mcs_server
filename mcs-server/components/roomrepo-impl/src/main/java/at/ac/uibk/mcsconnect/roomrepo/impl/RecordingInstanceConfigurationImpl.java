package at.ac.uibk.mcsconnect.roomrepo.impl;

import at.ac.uibk.mcsconnect.functional.osgi.OsgiPropertyReader;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstanceConfiguration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component(
        name = "at.ac.uibk.mcsconnect.roomservice.impl.RecordingInstanceConfiguration",
        immediate = true
)
public class RecordingInstanceConfigurationImpl implements RecordingInstanceConfiguration {

    private static final String CFG_MAX_STOP_TIME_THRESHOLD = "stop.time.extension.threshold";
    private static final String CFG_MAX_STOP_TIME_THRESHOLD_UNIT = "stop.time.extension.threshold.unit";

    private long stopTimeExtensionThreshold;
    private TimeUnit stopTimeExtensionThresholdUnit;

    @Activate
    public RecordingInstanceConfigurationImpl(Map<String, ?> props) {
        OsgiPropertyReader reader = OsgiPropertyReader.create(props);
        handleProps(reader);
    }

    private void handleProps(OsgiPropertyReader reader) {
        this.stopTimeExtensionThreshold = reader.getAsLong(CFG_MAX_STOP_TIME_THRESHOLD).getOrElse(RecordingInstanceConfigurationDefaults.STOP_TIME_EXTENSION_THRESHOLD);
        this.stopTimeExtensionThresholdUnit = reader.getAsTimeUnit(CFG_MAX_STOP_TIME_THRESHOLD_UNIT).getOrElse(RecordingInstanceConfigurationDefaults.STOP_TIME_EXTENSION_THRESHOLD_UNIT);
    }

    @Override
    public long getStopTimeExtensionThreshold() {
        return this.stopTimeExtensionThreshold;
    };

    @Override
    public TimeUnit getStopTimeExtensionThresholdUnit() {
        return this.stopTimeExtensionThresholdUnit;
    };

}
