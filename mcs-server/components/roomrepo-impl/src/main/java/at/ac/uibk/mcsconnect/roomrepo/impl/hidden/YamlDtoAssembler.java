package at.ac.uibk.mcsconnect.roomrepo.impl.hidden;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
import at.ac.uibk.mcsconnect.common.api.NetworkTargetFactory;
import at.ac.uibk.mcsconnect.executorservice.api.McsScheduledExecutorService;
import at.ac.uibk.mcsconnect.executorservice.api.McsSingletonExecutorService;
import at.ac.uibk.mcsconnect.functional.common.Function;
import at.ac.uibk.mcsconnect.functional.common.List;
import at.ac.uibk.mcsconnect.functional.common.Map;
import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.functional.common.Tuple;
import at.ac.uibk.mcsconnect.recorderservice.api.Recorder;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderFactory;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import at.ac.uibk.mcsconnect.roomrepo.api.RoomFactory;
import at.ac.uibk.mcsconnect.roomrepo.api.Terminal;
import at.ac.uibk.mcsconnect.roomrepo.api.TerminalFactory;
import at.ac.uibk.mcsconnect.roomrepo.impl.hidden.yamldto.NetworkTargetDTO;
import at.ac.uibk.mcsconnect.roomrepo.impl.hidden.yamldto.RecorderDTO;
import at.ac.uibk.mcsconnect.roomrepo.impl.hidden.yamldto.RoomDTO;
import at.ac.uibk.mcsconnect.roomrepo.impl.hidden.yamldto.RoomsDTO;
import at.ac.uibk.mcsconnect.roomrepo.impl.hidden.yamldto.TerminalDTO;
import at.ac.uibk.mcsconnect.sshsessionmanager.api.SshSessionManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static at.ac.uibk.mcsconnect.functional.common.List.sequence;

public final class YamlDtoAssembler {

    private static final Logger LOGGER = LoggerFactory.getLogger(YamlDtoAssembler.class);

    private Result<RoomFactory> roomFactory;
    private Result<RecorderFactory> recorderFactory;
    private Result<TerminalFactory> terminalFactory;
    private Result<NetworkTargetFactory> networkTargetFactory;
    private Result<SshSessionManagerService> sshSessionManagerService;
    private Result<McsScheduledExecutorService> mcsScheduledExecutorService;
    private Result<McsSingletonExecutorService> mcsSingletonExecutorService;

    public YamlDtoAssembler(
            RoomFactory roomFactory,
            RecorderFactory recorderFactory,
            TerminalFactory terminalFactory,
            NetworkTargetFactory networkTargetFactory,
            SshSessionManagerService sshSessionManagerService,
            McsScheduledExecutorService mcsScheduledExecutorService,
            McsSingletonExecutorService mcsSingletonExecutorService
    ) {
        this.roomFactory = Result.of(roomFactory, "roomFactory may not be null");
        this.recorderFactory = Result.of(recorderFactory, "recorderFactory may not be null");
        this.terminalFactory = Result.of(terminalFactory, "terminalFactory may not be null");
        this.networkTargetFactory = Result.of(networkTargetFactory, "networkTargetFactory may not be null");
        this.sshSessionManagerService = Result.of(sshSessionManagerService, "sshSessionManagerService may not be null");
        this.mcsScheduledExecutorService = Result.of(mcsScheduledExecutorService, "mcsScheduledExecutorService may not be null");
        this.mcsSingletonExecutorService = Result.of(mcsSingletonExecutorService, "mcsSingletonExecutorService may not be null");
    }

    public Result<Set<Terminal>> toTerminalSet(RoomDTO roomDTO) {
        Map<String, TerminalDTO> unsafeMap = Map.fromJavaMap(roomDTO.getTerminals());

        List<Tuple<String, TerminalDTO>> tuples = unsafeMap.entries();
        List<Result<Terminal>> uncheckedResult = tuples.map(e -> terminalDTOToTerminal(e));

        Result<Set<Terminal>> results = sequence(uncheckedResult).map(List::toJavaSet);

        results.forEachOrFail(r -> logInfo(String.format("Converted yaml to terminals: %s", r))).forEach(error -> String.format("Could not process all terminals from in-memory representation: %s, because %s", roomDTO, error));

        return results;
    }

    public Result<Set<Recorder>> toRecorderSet(RoomDTO roomDTO) { // TODO changed to Result<Set<Recorder>> because it should return a set or bust
        Map<String, RecorderDTO> unsafeMap = Map.fromJavaMap(roomDTO.getRecorders());
        List<Tuple<String, RecorderDTO>> tuples = unsafeMap.entries();
        List<Result<Recorder>> uncheckedResult = tuples.map(e -> recorderDTOToRecorder(e));


        Result<Set<Recorder>> results = sequence(uncheckedResult).map(List::toJavaSet);

        results.forEachOrFail(r -> logInfo(String.format("Converted recorders of room: %s", r))).forEach(error -> String.format("Could not process all recorders from in-memory representation: %s, because %s", roomDTO, error));

        return results;
    }

    private static Result<Integer> stringToInteger(String input) {
        Result<String> rInput = Result.of(input);
        return rInput.flatMap(s -> {
            try {
                return Result.success(Integer.valueOf(s));
            } catch (Exception e) {
                return Result.failure(String.format("Could not parse string \"%s\" as integer, because %s", input, e));
            }
        });
    }

    private static Result<String> liftString(String input) {
        Result<String> rInput = Result.of(input);
        return rInput.flatMap(s -> {
            try {
                return Result.success(s);
            } catch (Exception e) {
                return Result.failure(String.format("Could not parse string \"%s\", because %s", input, e));
            }
        });
    }

    public Set<Room> toRoomSet(RoomsDTO roomsDTO) { // TODO Return set of rooms or bust? Return set of rooms that were read in correctly?
        Map<String, RoomDTO> unsafeMap = Map.fromJavaMap(roomsDTO.getRooms());
        List<Tuple<String, RoomDTO>> tuples = unsafeMap.entries();
        List<Result<Room>> uncheckedResult = tuples.map(e -> roomDtoToRoom(e));

        Result<Set<Room>> results = sequence(uncheckedResult).map(List::toJavaSet);

        results.forEachOrFail(r -> logInfo(String.format("Converted yaml to room: %s", r))).forEach(error -> String.format("Could not process all rooms from in-memory representation: %s, because %s", roomsDTO, error));

        return results.isSuccess()
                ?  results.successValue()
                : new HashSet<>(); // TODO Decide how to handle failures: All or none? Some? In any case, log failures.
    }

    /** Operation: Tuple<String, RoomDTO> -> Result<Room> | string is the id (key in yaml) and RoomDTO is the value at that key. This function can be lifted for all entries. */
    private Result<Room> roomDtoToRoom(Tuple<String, RoomDTO> entry) {
        return Result.of(entry._1, String.format("id may not be null"))
                .flatMap(id -> Result.of(entry._2, String.format("value at id \"%s\" (the RoomDTO) may not be null", id))
                        .flatMap(roomDto -> getRoomNameResult.apply(roomDto)
                                .flatMap(name -> toRecorderSet(roomDto)
                                        .flatMap(recorders -> toTerminalSet(roomDto)
                                                .flatMap(terminals -> this.roomFactory
                                                        .map(roomFac -> roomFac.create(id, name, recorders, terminals)))))));
    }
    /** Utilites (migrated out of DTOs to conform to JavaBeans Specifications) */
    public static Function<RoomDTO, Result<String>> getRoomNameResult = x -> Result.of(x.getName(), "recorderDTO name may not be null");


    /** Operation: Tuple<String, RecorderDTO> -> Result<Recorder */
    private Result<Recorder> recorderDTOToRecorder(Tuple<String, RecorderDTO> entry) {
        return Result.of(entry._1, String.format("id may not be null"))
                .flatMap(id -> Result.of(entry._2, String.format("value at id \"%s\" (the RecorderDTO) may not be null", id))
                        .flatMap(recorderDto -> getRecorderName.apply(recorderDto)
                                .flatMap(name -> getRecorderTypeResult.apply(recorderDto)
                                    .flatMap(type -> networkTargetFactory
                                        .flatMap(ntFactory -> getRecorderNetworkTargetResult.apply(recorderDto)
                                                .flatMap(ntDto -> dtoToNetworkTarget(ntFactory, ntDto)
                                                        .flatMap(nt -> sshSessionManagerService
                                                            .flatMap(sshSessMangr -> mcsScheduledExecutorService
                                                                    .flatMap(schedExecService -> mcsSingletonExecutorService
                                                                            .flatMap(singleExecService -> recorderFactory
                                                                                    .map(recFactory -> recFactory.create(id, name, nt, sshSessMangr, schedExecService, singleExecService))))))))))));
    }

    /** Utilites (migrated out of DTOs to conform to JavaBeans Specifications) */
    public static Function<RecorderDTO, Result<String>> getRecorderName = x -> Result.of(x.getName(), "recorderDTO name may not be null");
    public static Function<RecorderDTO, Result<String>> getRecorderTypeResult = x -> Result.of(x.getName(), "recorderDTO type may not be null");
    public static Function<RecorderDTO, Result<String>> getRecorderNetworkTargetResult = x -> Result.of(x.getName(), "recorderDTO network target may not be null");

    /** Operation: Tuple<String, RecorderDTO> -> Result<Recorder */
    private Result<Terminal> terminalDTOToTerminal(Tuple<String, TerminalDTO> entry) {
        return Result.of(entry._1, String.format("id may not be null"))
                .flatMap(id -> Result.of(entry._2, String.format("value at id \"%s\" (the TerminalDTO) may not be null", id))
                        .flatMap(terminalDto -> getTerminalNameResult.apply(terminalDto)
                                .flatMap(name -> networkTargetFactory
                                    .flatMap(ntFactory -> getTerminalTargetResult.apply(terminalDto)
                                        .flatMap(ntDto -> dtoToNetworkTarget(ntFactory, ntDto)
                                                .flatMap(nt -> terminalFactory
                                                    .map(termFactory -> termFactory.create(id, name, nt))))))));
    }

    /** Utilites (migrated out of DTOs to conform to JavaBeans Specifications) */
    public static Function<TerminalDTO, Result<String>> getTerminalNameResult = x -> Result.of(x.getName(), "terminalDTO name may not be null");
    public static Function<TerminalDTO, Result<String>> getTerminalTargetResult = x -> Result.of(x.getTarget(), "terminalDTO target may not be null");


    /** Helper Network Target */
    private static Result<NetworkTarget> dtoToNetworkTarget(NetworkTargetFactory factory, NetworkTargetDTO dto) {
        return getHostResult.apply(dto)
                .flatMap(host -> getPortResult.apply(dto)
                    .flatMap(port -> getUsernameResult.apply(dto)
                        .flatMap(username -> getPasswordResult.apply(dto)
                                .flatMap(password -> factory.create(host, port, username, password)))));
    }
    /** Utilites (migrated out of DTOs to conform to JavaBeans Specifications) */
    public static Function<NetworkTargetDTO, Result<String>> getHostResult = x -> Result.of(x.getHost(), "networkTargetDTO host may not be null");
    public static Function<NetworkTargetDTO, Result<String>> getPortResult = x -> Result.of(x.getPort(), "networkTargetDTO port may not be null");
    public static Function<NetworkTargetDTO, Result<String>> getUsernameResult = x -> Result.of(x.getUsername(), "networkTargetDTO username may not be null");
    public static Function<NetworkTargetDTO, Result<String>> getPasswordResult = x -> Result.of(x.getPassword(), "networkTargetDTO password may not be null");

    private static Result<NetworkTarget> dtoToNetworkTarget(NetworkTargetFactory factory, String dto) {
        return Result.of(factory.create(dto));
    }

    private static void logInfo(String msg) {
        LOGGER.info(msg);
    }
    private static void logError(String msg) {
        LOGGER.error(msg);
    }

}
