package at.ac.uibk.mcsconnect.roomrepo.impl.hidden;

import at.ac.uibk.mcsconnect.common.api.NetworkTarget;
import at.ac.uibk.mcsconnect.common.api.NetworkTargetFactory;
import at.ac.uibk.mcsconnect.executorservice.api.McsScheduledExecutorService;
import at.ac.uibk.mcsconnect.executorservice.api.McsSingletonExecutorService;
import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.recorderservice.api.Recorder;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderFactory;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import at.ac.uibk.mcsconnect.roomrepo.api.RoomFactory;
import at.ac.uibk.mcsconnect.roomrepo.api.Terminal;
import at.ac.uibk.mcsconnect.roomrepo.api.TerminalFactory;
import at.ac.uibk.mcsconnect.roomrepo.impl.hidden.yamldto.RecorderDTO;
import at.ac.uibk.mcsconnect.roomrepo.impl.hidden.yamldto.RoomDTO;
import at.ac.uibk.mcsconnect.roomrepo.impl.hidden.yamldto.RoomsDTO;
import at.ac.uibk.mcsconnect.roomrepo.impl.hidden.yamldto.TerminalDTO;
import at.ac.uibk.mcsconnect.sshsessionmanager.api.SshSessionManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class YamlDtoAssembler {

    private static final Logger LOGGER = LoggerFactory.getLogger(YamlDtoAssembler.class);

    private RoomFactory roomFactory;
    private RecorderFactory recorderFactory;
    private TerminalFactory terminalFactory;
    private NetworkTargetFactory networkTargetFactory;
    private SshSessionManagerService sshSessionManagerService;
    private McsScheduledExecutorService mcsScheduledExecutorService;
    private McsSingletonExecutorService mcsSingletonExecutorService;

    public YamlDtoAssembler(
            RoomFactory roomFactory,
            RecorderFactory recorderFactory,
            TerminalFactory terminalFactory,
            NetworkTargetFactory networkTargetFactory,
            SshSessionManagerService sshSessionManagerService,
            McsScheduledExecutorService mcsScheduledExecutorService,
            McsSingletonExecutorService mcsSingletonExecutorService
    ) {
        this.roomFactory = roomFactory;
        this.recorderFactory = recorderFactory;
        this.terminalFactory = terminalFactory;
        this.networkTargetFactory = networkTargetFactory;
        this.sshSessionManagerService = sshSessionManagerService;
        this.mcsScheduledExecutorService = mcsScheduledExecutorService;
        this.mcsSingletonExecutorService = mcsSingletonExecutorService;
    }

    public Result<Set<Terminal>> toTerminalSet(RoomDTO roomDTO) {
        Result<RoomDTO> rRoomDTO = Result.of(roomDTO);
        return rRoomDTO.map(r -> r.getTerminals()).flatMap(terminals -> {
            Set<Terminal> output = new HashSet<>();
            for (Map.Entry<String, TerminalDTO> tdto : terminals.entrySet()) {
                Result<TerminalDTO> rTdto = Result.of(tdto.getValue(), String.format("Missing TerminalDTO: %s", tdto));
                Result<String> rId = Result.of(tdto.getKey(), String.format("Missing id in %s", tdto));
                Result<String> rName = rTdto.map(dto -> dto.getName());
                Result<NetworkTarget> rTarget = rTdto.map(dto -> dto.getTarget()).map(host -> networkTargetFactory.create(host));
                Result<Terminal> rTerminal = rId
                        .flatMap(id -> rName
                            .flatMap(name -> rTarget
                                .map(target -> this.terminalFactory.create(id, name, target))));
                if (rTerminal.isFailure()) return Result.failure(String.format("%s.toTerminalSet(%s) failed to parse: %s, because %s", this, roomDTO, tdto, rTerminal.failureValue()));
                output.add(rTerminal.successValue());
            }
            return Result.success(output);
        });
    }

    public Result<Set<Recorder>> toRecorderSet(RoomDTO roomDTO) { // TODO changed to Result<Set<Recorder>> because it should return a set or bust
        Result<RoomDTO> rRoomDTO = Result.of(roomDTO);
        return rRoomDTO.map(r -> r.getRecorders()).flatMap(recorders -> {
            Set<Recorder> output = new HashSet<>();
            for (Map.Entry<String, RecorderDTO> rdto : recorders.entrySet()) {
                Result<RecorderDTO> rDto = Result.of(rdto.getValue(),String.format("Missing RecorderDTO: %s", rdto));
                Result<String> rId = Result.of(rdto.getKey(), String.format("Missing id in %s", rdto));
                Result<String> rName = rDto.map(dto -> dto.getName());
                Result<NetworkTarget> rNetworkTarget = rDto
                        .flatMap(recorderDTO -> Result.of(recorderDTO.getTarget(), String.format("Could not get target in recorder dto: %s", recorderDTO))
                                .flatMap(targetDTO -> stringToInteger(targetDTO.getPort())
                                        .flatMap(port -> liftString(targetDTO.getHost())
                                                .flatMap(host -> liftString(targetDTO.getUsername()).flatMap(username -> liftString(targetDTO.getPassword())
                                                        .map(password -> networkTargetFactory.create(host, port, username, password)))))));
                Result<SshSessionManagerService> rSshSessionManagerService = Result.of(sshSessionManagerService, "SshSessionManagerService may not be null.");
                Result<McsScheduledExecutorService> rMcsScheduledExecutorService = Result.of(mcsScheduledExecutorService, "McsScheduledExecutorService may not be null.");
                Result<McsSingletonExecutorService> rMcsSingletonExecutorService = Result.of(mcsSingletonExecutorService, "McsSingletonExecutorService may not be null.");
                Result<RecorderFactory> rRecorderFactory = Result.of(this.recorderFactory, "RecorderFactory may not be null.");
                Result<Recorder> rRecorder= rId.flatMap(id -> rName
                        .flatMap(name -> rNetworkTarget
                                .flatMap(nt -> rSshSessionManagerService
                                        .flatMap(sshservice -> rMcsScheduledExecutorService
                                                .flatMap(mcsscheduled -> rMcsSingletonExecutorService
                                                        .flatMap(mcsexec -> rRecorderFactory
                                                            .map(recFactory -> recFactory.create(id, name, nt, sshservice, mcsscheduled, mcsexec))))))));
                if (rRecorder.isFailure()) return Result.failure(String.format("%s.toRecorderSet(%s) failed to parse: %, because %s", this, roomDTO, rdto, rRecorder.failureValue()));
                output.add(rRecorder.successValue());
            }
            return Result.success(output);
        });
    }

    private static Result<Integer> stringToInteger(String input) {
        Result<String> rInput = Result.of(input);
        return rInput.flatMap(s -> {
            try {
                return Result.success(Integer.valueOf(s));
            } catch (Exception e) {
                return Result.failure(String.format("Could not parse string \"%s\" as integer, because %s\n%s", input, e, e.getStackTrace()));
            }
        });
    }

    private static Result<String> liftString(String input) {
        Result<String> rInput = Result.of(input);
        return rInput.flatMap(s -> {
            try {
                return Result.success(s);
            } catch (Exception e) {
                return Result.failure(String.format("Could not parse string \"%s\", because %s\n%s", input, e, e.getStackTrace()));
            }
        });
    }

    public Result<Set<Result<Room>>> toRoomSet(RoomsDTO roomsDTO) {
        Result<RoomsDTO> rRoomsDTO = Result.of(roomsDTO);
        return rRoomsDTO.flatMap(rooms -> {
            Set<Result<Room>> output = new HashSet<>();
            for (Map.Entry<String, RoomDTO> entry : rooms.getRooms().entrySet()) {
                Result<String> rId = Result.of(entry.getKey(), String.format("Missing RecorderDTO: %s", entry));
                Result<RoomDTO> rDto = Result.of(entry.getValue(), String.format("Missing RoomDTO: %s", entry));
                Result<String> rName = rDto.map(dto -> dto.getName());
                Result<Set<Terminal>> rTerminals = rDto.flatMap(dto -> toTerminalSet(dto));
                Result<Set<Recorder>> rRecorders = rDto.flatMap(dto -> toRecorderSet(dto));
                Result<RoomFactory> rRoomFactory = Result.of(roomFactory, String.format("RoomFactory may not be null."));
                Result<Room> rRoom = rId.flatMap(id -> rName
                        .flatMap(name -> rTerminals
                                .flatMap(terminals -> rRecorders
                                        .flatMap(recorders -> rRoomFactory.map(roomFac -> roomFac.create(id, name, recorders, terminals))))));
                output.add(rRoom);
            }
            return Result.success(output);
        });
    }

    public static <T> Set<T> safeExtractSetResults(Set<Result<T>> input) {
            Set<T> output = new HashSet<>();
            for (Result<T> elem : input) {
                if (elem.isFailure()) YamlDtoAssembler.logError(String.format("safeExtractSetResults: %s", elem.failureValue()));
                elem.forEachOrFail(t -> output.add(t)).forEach(errMsg -> YamlDtoAssembler.logError(String.format("safeExtractSetResults failed with: %s", errMsg)));
            }
            return output;
    }
    public static <T> Set<T> safeExtractSetResults(Result<Set<Result<T>>> input) {

        Result<Set<T>> realOut = input.map(inputSet -> {
            Set<T> output = new HashSet<>();
            for (Result<T> elem : inputSet) {
                if (elem.isFailure()) YamlDtoAssembler.logError(String.format("safeExtractSetResults: %s", elem.failureValue()));
                elem.forEachOrFail(t -> output.add(t)).forEach(errMsg -> YamlDtoAssembler.logError(String.format("safeExtractSetResults failed with: %s", errMsg)));
            }
            return output;
        });
        return realOut.isSuccess()
                ? realOut.successValue()
                : new HashSet<>();
    }

    private static void logError(String msg) {
        LOGGER.error(msg);
    }

}
