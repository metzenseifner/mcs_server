package at.ac.uibk.mcsconnect.roomrepo.api;

import at.ac.uibk.mcsconnect.functional.common.Result;

import java.util.Set;
import java.util.function.Function;

/**
 * Provides access to all room resources at the University of Innsbruck.
 *
 * This is a registry, the primary abstraction of the university setup.
 * This is where we hide the repository for rooms.
 */
public interface RoomRepo {

    /**
     * Get a {@link Room} from the registry based on a reference id.
     *
     * @param id
     * @return
     */
    Result<Room> get(String id);

    /**
     * Populate registry with {@link Room}s.
     *
     * @param room
     * @return
     */
    Result<Boolean> add(Room room);
    Result<Boolean> remove(String id);
    
    Set<Room> getRooms();
    Set<Room> getRoomsByFilter(Function<Room, Boolean> filter);

    Result<Room> getRoomForHost(String host);
}
