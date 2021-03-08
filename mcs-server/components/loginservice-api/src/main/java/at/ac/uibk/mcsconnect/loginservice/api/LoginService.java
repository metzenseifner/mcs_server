package at.ac.uibk.mcsconnect.loginservice.api;

import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.person.api.User;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;

/**
 * Unused idea for future design
 */
public interface LoginService {

    Result<String> register(User user, Room room);

    Result<String> unregister(User user, Room room);

    Result<Room> getRoomForUser(User user);

    Result<User> getUserAt(Room room);

}
