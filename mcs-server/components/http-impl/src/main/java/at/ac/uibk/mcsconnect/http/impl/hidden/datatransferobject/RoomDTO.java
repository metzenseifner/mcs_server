package at.ac.uibk.mcsconnect.http.impl.hidden.datatransferobject;


import at.ac.uibk.mcsconnect.common.api.Immutable;
import at.ac.uibk.mcsconnect.common.api.Views;
import at.ac.uibk.mcsconnect.http.api.DTO;
import com.fasterxml.jackson.annotation.JsonView;

import java.util.Optional;

@Immutable
public class RoomDTO implements DTO {

    @JsonView(Views.Public.class)
    public final String id;
    @JsonView(Views.Public.class)
    public final String name;
    @JsonView(Views.Public.class)
    public final String polledRecordingRunningState;
    @JsonView(Views.Public.class)
    public final Optional<RecordingInstanceDTO> recordingInstance;

    public RoomDTO(String id,
                   String name,
                   String polledRecordingRunningState,
                   Optional<RecordingInstanceDTO> recordingInstance) {
        this.id = id;
        this.name = name;
        this.polledRecordingRunningState = polledRecordingRunningState;
        this.recordingInstance = recordingInstance;
    }

}
