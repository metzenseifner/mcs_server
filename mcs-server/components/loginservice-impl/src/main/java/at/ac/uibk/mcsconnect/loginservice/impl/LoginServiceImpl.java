package at.ac.uibk.mcsconnect.loginservice.impl;

import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.functional.common.Tuple;
import at.ac.uibk.mcsconnect.loginservice.api.LoginService;
import at.ac.uibk.mcsconnect.person.api.User;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import org.osgi.service.component.annotations.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Component(
        name = "at.ac.uibk.mcsconnect.loginmanager.impl.LoginServiceImpl",
        immediate = true
)
public class LoginServiceImpl implements LoginService {

    private final List<Tuple<User, Room>> registry = new LinkedList<>();

    public LoginServiceImpl() {}

    @Override
    public synchronized Result<String> register(User user, Room room) {
        try {
            registry.add(new Tuple<>(user, room));
            return Result.success(String.format("Successfully registered: (%s, %s)", user, room));
        } catch (Exception e) {
            return Result.failure(String.format("Failed to register: (%s, %s)", user, room), e);
        }
    }

    @Override
    public synchronized Result<String> unregister(User user, Room room) {
        try {
            Optional<Tuple<User, Room>> oResult = registry.stream().filter(t -> !t._1.equals(user)).findFirst();
            registry.remove(oResult.get());
            return Result.success(String.format("Successfully unregistered: (%s, %s)", user, room));
        } catch (Exception e) {
            return Result.failure(String.format("Failed to unregister: (%s, %s)", user, room), e);
        }
    }

    @Override
    public synchronized Result<Room> getRoomForUser(User user) {
        try {
            Optional<Tuple<User, Room>> oResult = registry.stream().filter(t -> t._1.equals(user)).findFirst();
            return Result.of(oResult.get()._2);
        } catch (Exception e) {
            return Result.failure(String.format("Could not find room for user: %s", user), e);
        }
    }

    @Override
    public synchronized Result<User> getUserAt(Room room) {
        try {
            Optional<Tuple<User, Room>> oResult = registry.stream().filter(t -> t._2.equals(room)).findFirst();
            return Result.of(oResult.get()._1);
        } catch (Exception e) {
            return Result.failure(String.format("Could not find user for room: %s", room), e);
        }
    }
}
