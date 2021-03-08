package at.ac.uibk.mcsconnect.http.impl.hidden.datatransferobject;

import at.ac.uibk.mcsconnect.common.api.Immutable;
import at.ac.uibk.mcsconnect.common.api.Views;
import at.ac.uibk.mcsconnect.http.api.DTO;
import com.fasterxml.jackson.annotation.JsonView;

import java.time.LocalDateTime;

@Immutable
public class RecordingInstanceDTO implements DTO {

    @JsonView(Views.Public.class)
    public final BookingDTO booking;
    @JsonView(Views.Public.class)
    public final LocalDateTime stopTime;
    @JsonView(Views.Public.class)
    public final String recordingName;
    @JsonView(Views.Public.class)
    public final String recordingRunningState;

    public RecordingInstanceDTO(BookingDTO booking, LocalDateTime stopTime, String recordingName, String recordingRunningState) {
        this.booking = booking;
        this.stopTime = stopTime;
        this.recordingName = recordingName;
        this.recordingRunningState = recordingRunningState;
    }
}
