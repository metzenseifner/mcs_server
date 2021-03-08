package at.ac.uibk.mcsconnect.roomrepo.impl.hidden;

import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.recorderservice.api.SisProtocol;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstance;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.TimerTask;

/**
 * Thread task that handles stopping a recording and resetting a {@link Room}'s
 * {@link RecordingInstance} to {@link Optional#empty()}.
 */
public class TaskStopRecording extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskStopRecording.class);

    // This reference should disappear when a change of room configuration is triggered
    // which is relevant, because a scheduled task might outlive a room configuration.
    private final Room room;
    private final RecordingInstance recordingInstance;

    public TaskStopRecording(Room room, RecordingInstance recordingInstance) {
        this.room = room;
        this.recordingInstance = recordingInstance;
    }

    public void run() {
     // TODO: Replace with cleaner functional code
     // if (room.getRecordingInstance().isPresent()) {
     //     if (room.getRecordingInstance().get() == recordingInstance) {
     //         LOGGER.info(String.format("%s stopping %s", this, recordingInstance));
     //         room.getRecorders().parallelStream().forEach(r -> r.sendMessage(SisProtocol.Command.STOPRECORDING));
     //         LOGGER.info(String.format("%s setting recording instance to empty.", this));
     //         room.setRecordingInstance(Optional.empty());
     //     }
     // }

        Result.of(room)
                .filter(ro -> ro.getRecordingInstance().equals(recordingInstance))
                .map(ro -> ro.getRecorders())
                .forEach(rs -> rs.stream().forEach(r -> r.sendMessage(SisProtocol.Command.STOPRECORDING)));
        room.setRecordingInstance(Optional.empty());
    }

    @Override
    public String toString() {
        return String.format("%s.(%s)", getClass().getSimpleName(), room);
    }
}