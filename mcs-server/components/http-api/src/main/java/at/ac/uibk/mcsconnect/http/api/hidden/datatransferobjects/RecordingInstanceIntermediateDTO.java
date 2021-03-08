package at.ac.uibk.mcsconnect.http.api.hidden.datatransferobjects;

import at.ac.uibk.mcsconnect.http.api.DTO;
import at.ac.uibk.mcsconnect.http.api.RecordingInstanceIntermediatePost;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Deserialization of POST calls requires a dependency on a concrete object.
 *
 * Note that there are no null checks.
 *
 * TODO The original class RecordingInstanceIntermediate knew how to apply itself to an existing RecordingInstance
 * and return a new immutable object properly.
 */
public class RecordingInstanceIntermediateDTO implements DTO, RecordingInstanceIntermediatePost {

    @Schema(required = true, description = "The ID of a booking. It must be provided on the first call, but may be left out on subsequent calls.")
    public final String bookingId;
    @Schema(required = false, allowableValues = "The stop time must not exceed the maximum time limit.", description = "The stop time according to the ISO 8601 instant format without the time zone identifier, because the university database engineers decided not to include time zones!")
    public final String stopTime;
    @Schema(required = false, description = "An optional recording name to overwrite the default name.")
    public final String recordingName;
    @Schema(required = false, description = "The running state acts as a switch between states {STOPPED, RECORDING}. Once the state RECORDING has been set, nothing else may be changed.")
    public final String recordingRunningState;

    /** Null values are allowed here! Decision made up the stack */
    public RecordingInstanceIntermediateDTO(@JsonProperty("bookingId") String bookingId,
                                            @JsonProperty("stopTime") String stopTime,
                                            @JsonProperty("recordingName") String recordingName,
                                            @JsonProperty("recordingRunningState") String recordingRunningState) {
        this.bookingId = bookingId;
        this.stopTime = stopTime;
        this.recordingName = recordingName;
        this.recordingRunningState = recordingRunningState;
    }

    @Override
    public String getBookingId() {
        return this.bookingId;
    }

    @Override
    public String getStopTime() {
        return this.stopTime;
    }

    @Override
    public String getRecordingName() {
        return this.recordingName;
    }

    @Override
    public String getRecordingRunningState() {
        return this.recordingRunningState;
    }

    @Override
    public String toString() {
        return String.format("%s(%s, %s, %s, %s)",
                this.getClass().getSimpleName(),
                this.bookingId,
                this.stopTime,
                this.recordingName,
                this.recordingRunningState);
    }
}
