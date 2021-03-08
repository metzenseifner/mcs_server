package at.ac.uibk.mcsconnect.roomrepo.impl;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
import at.ac.uibk.mcsconnect.common.api.NetworkTargetFactory;
import at.ac.uibk.mcsconnect.executorservice.api.McsScheduledExecutorService;
import at.ac.uibk.mcsconnect.executorservice.api.McsSingletonExecutorService;
import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.recorderservice.api.Recorder;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderFactory;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import at.ac.uibk.mcsconnect.roomrepo.api.RoomFactory;
import at.ac.uibk.mcsconnect.roomrepo.api.RoomRepo;
import at.ac.uibk.mcsconnect.roomrepo.api.Terminal;
import at.ac.uibk.mcsconnect.roomrepo.api.TerminalFactory;
import at.ac.uibk.mcsconnect.sshsessionmanager.api.SshSessionManagerService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

//@Component(
//        name = "at.ac.uibk.mcsconnect.roomservice.impl.RoomServiceInMemoryImpl",
//        scope = ServiceScope.SINGLETON,
//        immediate = true
//)
public class RoomRepoInMemoryImpl implements RoomRepo {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoomRepoInMemoryImpl.class);

    private Map<String, Room> registry = new HashMap<>();

    private RoomFactory roomFactory;
    private RecorderFactory recorderFactory;
    private TerminalFactory terminalFactory;
    private NetworkTargetFactory networkTargetFactory;
    private SshSessionManagerService sshSessionManagerService;
    private McsScheduledExecutorService mcsScheduledExecutorService;
    private McsSingletonExecutorService mcsSingletonExecutorService;



    @Activate
    public RoomRepoInMemoryImpl(
            @Reference RoomFactory roomFactory,
            @Reference RecorderFactory recorderFactory,
            @Reference TerminalFactory terminalFactory,
            @Reference NetworkTargetFactory networkTargetFactory,
            @Reference SshSessionManagerService sshSessionManagerService,
            @Reference McsScheduledExecutorService mcsScheduledExecutorService,
            @Reference McsSingletonExecutorService mcsSingletonExecutorService
    ) {
        this.roomFactory = roomFactory;
        this.recorderFactory = recorderFactory;
        this.terminalFactory = terminalFactory;
        this.networkTargetFactory = networkTargetFactory;
        this.sshSessionManagerService = sshSessionManagerService;
        this.mcsScheduledExecutorService = mcsScheduledExecutorService;
        this.mcsSingletonExecutorService = mcsSingletonExecutorService;
        setup();
    }

    public void setup() {
        // init recorders
        Set<Recorder> recorders = new HashSet<>();
        NetworkTarget main_nt = networkTargetFactory.create("138.232.11.205", 22023, "abc", "");
        NetworkTarget side_nt = networkTargetFactory.create("138.232.11.2", 22023, "abc", "");
        SshSessionManagerService sshSessionManagerService = this.sshSessionManagerService;
        recorders.add(recorderFactory.create("r_avstudio_01", "Main", main_nt, sshSessionManagerService, mcsScheduledExecutorService, mcsSingletonExecutorService));
        recorders.add(recorderFactory.create("r_avstudio_02", "Side", side_nt, sshSessionManagerService, mcsScheduledExecutorService, mcsSingletonExecutorService));

        // init terminals
        Set<Terminal> terminals = new HashSet<>();
        terminals.add(terminalFactory.create("localhost", "localhost", networkTargetFactory.create("127.0.0.1")));

        // init rooms (id -> Room) for O(1) performance
        Map<String, Room> tempRoomMap = new HashMap<>();
        tempRoomMap.put("avstudio", roomFactory.create("avstudio", "AV Studio", recorders, terminals));
        this.registry = tempRoomMap;
    }

    @Override
    public Result<Room> get(String id) {
        try {
            return Result.of(this.registry.get(id));
        } catch (Exception e) {
            return Result.failure(String.format("Could not get key \"%s\" from map: %s", this.registry));
        }
    }

    @Override
    public Result<Boolean> add(Room room) {
        try {
            this.registry.put(room.getId(), room);
            return Result.success(true);
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    @Override
    public Result<Boolean> remove(String id) {
        try {
            Result<Room> room = get(id);
            room.forEachOrFail(r -> this.registry.remove(r))
                    .forEach(e -> LOGGER.error(String.format("%s.remove(%s) failed, because: %s", this.getClass().getSimpleName(), id, e)));
            return Result.success(true);
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    @Override
    public Set<Room> getRooms() {
        return Collections.unmodifiableSet(this.registry.values().stream().collect(Collectors.toSet()));
    }

    @Override
    public Set<Room> getRoomsByFilter(Function<Room, Boolean> filter) {
        try {
            return Collections.unmodifiableSet(this.registry.values().stream()
                    .filter(r -> filter.apply(r))
                    .collect(Collectors.toSet()));
        } catch (Exception e) {
            return new HashSet<>();
        }
    }

    @Override
    public Result<Room> getRoomForHost(String host) {
        for (Room r : this.registry.values()) {
            Set<Terminal> terms = r.getTerminals();
            for (Terminal term : terms) {
                 if (term.getNetworkTarget().getHost().equals(host)) return Result.success(r);
                }
            }
        return Result.failure(String.format("Host not assigned to a room: %s", host));
        }

}
