package at.ac.uibk.mcsconnect.roomrepo.impl;

import at.ac.uibk.mcsconnect.common.api.NetworkTargetFactory;
import at.ac.uibk.mcsconnect.common.api.OsgiProperties;
import at.ac.uibk.mcsconnect.common.api.OsgiProperty;
import at.ac.uibk.mcsconnect.common.api.Views;
import at.ac.uibk.mcsconnect.executorservice.api.McsScheduledExecutorService;
import at.ac.uibk.mcsconnect.executorservice.api.McsSingletonExecutorService;
import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderFactory;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import at.ac.uibk.mcsconnect.roomrepo.api.RoomFactory;
import at.ac.uibk.mcsconnect.roomrepo.api.RoomRepo;
import at.ac.uibk.mcsconnect.roomrepo.api.Terminal;
import at.ac.uibk.mcsconnect.roomrepo.api.TerminalFactory;
import at.ac.uibk.mcsconnect.roomrepo.impl.hidden.YamlDtoAssembler;
import at.ac.uibk.mcsconnect.roomrepo.impl.hidden.yamldto.RoomsDTO;
import at.ac.uibk.mcsconnect.sshsessionmanager.api.SshSessionManagerService;
import com.fasterxml.jackson.annotation.JsonView;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Supports live changes to the university's room topology.
 */
@Component(
        name = "at.ac.uibk.mcsconnect.roomrepo.impl.RoomRepoYamlImpl",
        immediate = true
)
public class RoomRepoYamlImpl implements RoomRepo {

    // volatile ensures compile does not pull value from cpu cache, rather from main memory such that all reads happen after writes are completed
    //private static volatile RoomServiceImpl instance = null;
    private Set<Room> registry = new HashSet<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(RoomRepoYamlImpl.class);

    // config keys
    private static final String CFG_ROOMS_YML_DIR = "rooms.path";

    // defaults
    private Path DEFAULT_ROOMS_PATH = Paths.get(Optional.ofNullable(System.getenv("KARAF_ETC")).orElse("/usr/local/karaf/etc"), "rooms");
    ;

    private final OsgiProperties osgiProperties = OsgiProperties.create(
            OsgiProperty.create(CFG_ROOMS_YML_DIR,
                    s -> r -> r.getAsPath(s).getOrElse(DEFAULT_ROOMS_PATH),
                    this::setRoomsDir)
    );

    // helpers
    private static final String SUCCESS_MESSAGE_FORMATTER = "Setting \"%s\" to: \"%s\"";

    private Path roomsDir;

    private YamlDtoAssembler yamlDtoAssembler;

    RoomFactory roomFactory;
    RecorderFactory recorderFactory;
    TerminalFactory terminalFactory;
    NetworkTargetFactory networkTargetFactory;
    SshSessionManagerService sshSessionManagerService;
    McsScheduledExecutorService mcsScheduledExecutorService;
    McsSingletonExecutorService mcsSingletonExecutorService;

    /**
     * Constructor
     */
    @Activate
    public RoomRepoYamlImpl(
            Map<String, ?> props,
            @Reference RoomFactory roomFactory,
            @Reference RecorderFactory recorderFactory,
            @Reference TerminalFactory terminalFactory,
            @Reference NetworkTargetFactory networkTargetFactory,
            @Reference SshSessionManagerService sshSessionManagerService,
            @Reference McsScheduledExecutorService mcsScheduledExecutorService,
            @Reference McsSingletonExecutorService mcsSingletonExecutorService) {
        this.yamlDtoAssembler = new YamlDtoAssembler(
                roomFactory,
                recorderFactory,
                terminalFactory,
                networkTargetFactory,
                sshSessionManagerService,
                mcsScheduledExecutorService,
                mcsSingletonExecutorService);
        this.roomFactory = roomFactory;
        this.recorderFactory = recorderFactory;
        this.terminalFactory = terminalFactory;
        this.networkTargetFactory = networkTargetFactory;
        this.sshSessionManagerService = sshSessionManagerService;
        this.mcsScheduledExecutorService = mcsScheduledExecutorService;
        this.mcsSingletonExecutorService = mcsSingletonExecutorService;
    }

    @Activate
    public void activate(Map<String, ?> props) {
        LOGGER.info(String.format("%s.activate() called", this));
        handleProps(props);
    }

    @Modified
    public void modified(Map<String, ?> props) {
        LOGGER.debug(String.format("%s.modified() called", this));
        handleProps(props);
    }

    @Deactivate
    public void deactivate() {
        LOGGER.info(String.format("%s.deactivate() called", this));
        cleanRegistry();
    }

    /**
     * Algorithm must also remove old rooms (remember, its recorders have threads) cleanly.
     *
     * In particular, the scheduled threads must be cancelled.
     *
     * @param properties
     */
    private void handleProps(Map<String, ?> properties) {
        cleanRegistry();

        osgiProperties.resolve(properties, OsgiProperties.LogLevel.INFO);

        // TODO replace all of this with a functional reader
        LOGGER.info(String.format("%s reading in rooms from: %s", this, this.roomsDir));
        try (Stream<Path> stream = Files.list(this.roomsDir)) {
            Set<Path> roomFiles = stream
                    .filter(p -> Files.isRegularFile(p))
                    .filter(p -> p.getFileName().toString().endsWith(".yml") || p.getFileName().toString().endsWith(".yaml"))
                    .collect(Collectors.toSet());
            LOGGER.info(String.format("Found %s rooms: %s", roomFiles.size(), roomFiles));

            for (Path p : roomFiles) {
                LOGGER.info(String.format("Processing room yaml at: %s", p));
                try (InputStream inputStream = new FileInputStream(p.toFile())) {
                    ;
                    /** exception=Class not found even when on classpath, see https://stackoverflow.com/questions/26463078/snakeyaml-class-not-found-exception */
                    //Yaml yamlRoomsDTOParser = new Yaml(new Constructor(RoomsDTO.class));
                    Yaml yamlRoomsDTOParser = new Yaml(new CustomClassLoaderConstructor(RoomsDTO.class.getClassLoader()));
                    RoomsDTO roomsDTO = yamlRoomsDTOParser.loadAs(inputStream, RoomsDTO.class);
                    Set<Room> rooms = yamlDtoAssembler.toRoomSet(roomsDTO);
                    LOGGER.info(String.format("Created rooms: %s", rooms));
                    rooms.stream()
                            .peek(r -> LOGGER.info(String.format("%s adding room to registry: %s", this, r.toFullString())))
                            .forEach(r -> this.add(r));
                } catch (IOException i) {
                    LOGGER.error(String.format("IO error while trying to read rooms path \"%s\", because %s", this.roomsDir, i));
                }
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error while reading rooms path \"%s\", because %s", this.roomsDir, e));
        }
    }

    /**
     * Reset registry
     *
     */
    private void cleanRegistry() {
        LOGGER.debug(String.format("%s.cleanRegistry() called.", this.getClass().getSimpleName()));
        for (Room r : registry) {
            LOGGER.info(String.format("%s destroying: %s", this.getClass().getSimpleName(), r));
            r.destruct(); // Handles safe removal of threads
            remove(r.getId());
        }
        this.registry = new HashSet<>(); // wipe out old refs
    }

    /**
     * Get a {@link Room} from the registry based on a reference id.
     *
     * @param id
     * @return
     */
    public Result<Room> get(String id) {
        Optional<Room> oRoom = getRooms().stream().filter(r -> r.getId().equals(id)).findFirst();
        try {
            return Result.of(oRoom.get());
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    ;

    /**
     * Populate registry with {@link Room}s.
     *
     * @param room
     * @return
     */
    public Result<Boolean> add(Room room) {
        try {
            LOGGER.info(String.format("%s.add(%s)", this, room.toFullString()));
            this.registry.add(room);
            return Result.success(true);
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public Result<Boolean> remove(String id) {
        try {
            LOGGER.info(String.format("%s.remove(%s) called.", this, id));
            Result<Room> room = get(id);
            room.forEachOrFail(r -> this.registry.remove(r))
                    .forEach(e -> LOGGER.error(String.format("%s.remove(%s) failed, because: %s", this.getClass().getSimpleName(), id, e)));
            return Result.success(true);
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    @JsonView(Views.Admin.class)
    public Set<Room> getRooms() {
        return Collections.unmodifiableSet(this.registry);
    }

    //@JsonView(Views.Admin.class)
    //public Result<Room> getRoom(String id) {
    //    for (Room room : registry) {
    //        if (room.getId().equals(id)) {
    //            LOGGER.debug(String.format("%s.getLocation(%s) found match: %s", this, id, room));
    //            return Result.success(room);
    //        }
    //    }
    //    return Result.failure(new RuntimeException(String.format("%s.getLocation(%s) failed to find a match.", this, id)));
    //}

    public Set<Room> getRoomsByFilter(Function<Room, Boolean> filter) {
        Set<Room> result = new HashSet<>();
        for (Room r : getRooms()) {
            if (filter.apply(r)) {
                result.add(r);
            }
            ;
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public String toString() {
        return String.format("%s", this.getClass().getSimpleName());
    }

    public Set<String> getTerminalIPAddresses() {
        return getRooms()
                .parallelStream()
                .flatMap(r -> r.getTerminals().parallelStream()
                        .map(term -> term.getNetworkTarget().getHost())).collect(Collectors.toSet());
    }

    // f := Set<Room> -> Set<Terminal> -> NetworkTarget -> String(host/ip) -> UibkTerminal
    public Optional<Terminal> getTerminalByHost(String host) {
        // TODO someday a good exercise in functional programming

        for (Terminal t : getTerminals()) {
            if (t.getNetworkTarget().getHost().equals(host)) return Optional.of(t);
        }
        return Optional.empty();
    }

    public Set<Terminal> getTerminals() {
        return getRooms().parallelStream().map(Room::getTerminals).flatMap(Set::stream).collect(Collectors.toSet());
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

    public void setRoomsDir(Path roomsDir) {
        this.roomsDir = roomsDir;
    }
}
