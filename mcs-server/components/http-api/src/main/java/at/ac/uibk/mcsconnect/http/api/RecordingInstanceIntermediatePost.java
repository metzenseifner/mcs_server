package at.ac.uibk.mcsconnect.http.api;

import at.ac.uibk.mcsconnect.http.api.hidden.datatransferobjects.RecordingInstanceIntermediateDTO;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * This class contains client-controllable members of a
 * recording instance.
 */
@JsonDeserialize(as = RecordingInstanceIntermediateDTO.class)
public interface RecordingInstanceIntermediatePost {

    @Schema(required = true, description = "The ID of a booking. It must be provided on the first call, but may be left out on subsequent calls.")
    String getBookingId();
    @Schema(required = false, allowableValues = "The stop time must not exceed the maximum time limit.", description = "The stop time according to the ISO 8601 instant format without the time zone identifier, because the university database engineers decided not to include time zones!")
    String getStopTime();
    @Schema(required = false, description = "An optional recording name to overwrite the default name.")
    String getRecordingName();
    @Schema(required = false, description = "The running state acts as a switch between states {STOPPED, RECORDING}. Once the state RECORDING has been set, nothing else may be changed.")
    String getRecordingRunningState();

}
