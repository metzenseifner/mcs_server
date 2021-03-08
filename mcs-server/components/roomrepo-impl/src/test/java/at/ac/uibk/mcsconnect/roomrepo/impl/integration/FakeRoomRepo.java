package at.ac.uibk.mcsconnect.roomrepo.impl.integration;

import at.ac.uibk.mcsconnect.common.api.MemUtils;
import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import at.ac.uibk.mcsconnect.roomrepo.api.RoomRepo;
import at.ac.uibk.mcsconnect.roomrepo.api.Terminal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FakeRoomRepo implements RoomRepo {

    private Set<Room> rooms;

    public FakeRoomRepo() {
        this.rooms = new HashSet<>();
    }
    public FakeRoomRepo(Room room) {
        this.rooms = new HashSet<>();
        this.rooms.add(room);
    }
    public FakeRoomRepo(Set<Room> rooms) {
        this.rooms = rooms;
    }
    public FakeRoomRepo(Room... rooms) {
        MemUtils.nullCheck(rooms);
        this.rooms = Arrays.stream(rooms).collect(Collectors.toSet());
    }

    @Override
    public Result<Room> get(String id) {
        Optional<Room> oRoom = rooms.stream().filter(r -> r.getId().equals(id)).findFirst();
        return oRoom.isPresent()
                ? Result.success(oRoom.get())
                : Result.failure(String.format("Could not find room with id %s", id));
    }

    @Override
    public Result<Boolean> add(Room room) {
        try {
            this.rooms.add(room);
            return Result.success(true);
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    @Override
    public Result<Boolean> remove(String id) {
        try {
            Result<Room> room = get(id);
            room.forEachOrFail(r -> this.rooms.remove(r));
            return Result.success(true);
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    @Override
    public Set<Room> getRooms() {
        return Collections.unmodifiableSet(this.rooms);
    }

    @Override
    public Set<Room> getRoomsByFilter(Function<Room, Boolean> filter) {
        return Collections.unmodifiableSet(this.rooms.stream().filter(r -> filter.apply(r)).collect(Collectors.toSet()));
    }

    public Result<Room> getRoomForHost(String host) {
        Set<Room> rooms = getRoomsByFilter(r -> r.hasTerminals());
        for (Room r : rooms) {
            Set<Terminal> terms = r.getTerminals();
            for (Terminal term : terms) {
                if (term.getNetworkTarget().getHost().equals(host)) return Result.success(r);
            }
        }
        return Result.failure(String.format("Host not assigned to a room: %s", host));
    }
}
