package at.ac.uibk.mcsconnect.roomrepo.impl.integration;

import at.ac.uibk.mcsconnect.recorderservice.api.Recorder;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import at.ac.uibk.mcsconnect.roomrepo.api.RoomFactory;
import at.ac.uibk.mcsconnect.roomrepo.api.Terminal;

import java.util.Set;

public class FakeRoomFactory implements RoomFactory {
    @Override
    public Room create(String id, String name, Set<Recorder> recorders, Set<Terminal> terminals) {
        return FakeRoom.create(id, name, recorders, terminals);
    }
}
