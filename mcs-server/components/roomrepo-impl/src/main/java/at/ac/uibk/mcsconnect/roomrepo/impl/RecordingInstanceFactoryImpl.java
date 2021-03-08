package at.ac.uibk.mcsconnect.roomrepo.impl;

import at.ac.uibk.mcsconnect.bookingrepo.api.Booking;
import at.ac.uibk.mcsconnect.common.api.MemUtils;
import at.ac.uibk.mcsconnect.person.api.User;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderRunningStatesEnum;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstance;
import at.ac.uibk.mcsconnect.roomrepo.api.RecordingInstanceFactory;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import at.ac.uibk.mcsconnect.roomrepo.impl.hidden.RecordingInstanceImpl;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.time.LocalDateTime;


@Component(
        name = "at.ac.uibk.mcsconnect.roomservice.impl.RecordingInstanceFactoryImpl",
        immediate = true
)
public class RecordingInstanceFactoryImpl implements RecordingInstanceFactory {

    /**
     * Singleton instance of this factory
     */
    private static final RecordingInstanceFactoryImpl factory = new RecordingInstanceFactoryImpl();

    /**
     * Private constructor to create a new builder factory.
     */
    @Activate
    public RecordingInstanceFactoryImpl() {
    }

    public RecordingInstance create(
            Room room,
            User user,
            Booking booking,
            //Set<Recorder> observers, // TODO removed from new version. Room is aggregate for recorders
            LocalDateTime stopTime,
            String recordingName,
            RecorderRunningStatesEnum recorderRunningStatesEnum
    ) {

        return RecordingInstanceImpl.apply(MemUtils.nullCheck(room), MemUtils.nullCheck(user), MemUtils.nullCheck(booking), MemUtils.nullCheck(room.getRecorders()), MemUtils.nullCheck(stopTime), MemUtils.nullCheck(recordingName), MemUtils.nullCheck(recorderRunningStatesEnum));
    }


}
