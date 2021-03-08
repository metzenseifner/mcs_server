package at.ac.uibk.mcsconnect.roomrepo.api;

import at.ac.uibk.mcsconnect.recorderservice.api.Recorder;

import java.util.Set;

/**
 * This factory instantiates {@link Room} objects.
 */
public interface RoomFactory {

    Room create(
            String id,
            String name,
            Set<Recorder> recorders,
            Set<Terminal> terminals
    );

}
