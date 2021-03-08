package at.ac.uibk.mcsconnect.roomrepo.impl;

import at.ac.uibk.mcsconnect.recorderservice.api.Recorder;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import at.ac.uibk.mcsconnect.roomrepo.api.RoomFactory;
import at.ac.uibk.mcsconnect.roomrepo.api.Terminal;
import at.ac.uibk.mcsconnect.roomrepo.impl.hidden.UibkRoom;
import org.osgi.service.component.annotations.Component;

import java.util.Set;

@Component(
        name = "at.ac.uibk.mcsconnect.roomservice.impl.RoomFactoryUibkRoomImpl"
)
public class RoomFactoryUibkRoomImpl implements RoomFactory {

    @Override
    public Room create(String id, String name, Set<Recorder> recorders, Set<Terminal> terminals) {
        return UibkRoom.create(id, name, recorders, terminals);
    }
}
