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

    public Result<Set<Result<Terminal>>> toTerminalSet(RoomDTO roomDTO) {
        Result<RoomDTO> rRoomDTO = Result.of(roomDTO);
        return rRoomDTO.map(r -> r.getTerminals()).flatMap(terminals -> {
            Set<Result<Terminal>> output = new HashSet<>();
            for (Map.Entry<String, TerminalDTO> tdto : terminals.entrySet()) {
                Result<TerminalDTO> rTdto = Result.of(tdto.getValue(), String.format("Missing TerminalDTO: %s", tdto));
                Result<String> rId = Result.of(tdto.getKey(), String.format("Missing id in %s", tdto));
                Result<String> rName = rTdto.map(dto -> dto.getName());
                Result<NetworkTarget> rTarget = rTdto.map(dto -> dto.getTarget()).map(host -> networkTargetFactory.create(host));
                Result<Terminal> rTerminal = rId
                        .flatMap(id -> rName
                            .flatMap(name -> rTarget
                                .map(target -> this.terminalFactory.create(id, name, target))));
                output.add(rTerminal);
            }
            return Result.success(output);
        });
    }

    public Result<Set<Result<Recorder>>> toRecorderSet(RoomDTO roomDTO) {
        Result<RoomDTO> rRoomDTO = Result.of(roomDTO);
        return rRoomDTO.map(r -> r.getRecorders()).flatMap(recorders -> {
            Set<Result<Recorder>> output = new HashSet<>();
            for (Map.Entry<String, RecorderDTO> rEntry : recorders.entrySet()) {
                Result<RecorderDTO> rDto = Result.of(rEntry.getValue(),String.format("Missing RecorderDTO: %s", rEntry));
                Result<String> rId = Result.of(rEntry.getKey(), String.format("Missing id in %s", rEntry));
                Result<String> rName = rDto.map(dto -> dto.getName());
                Result<NetworkTarget> rNetworkTarget = rDto.map(dto -> dto.getTarget()).map(ntDTO -> networkTargetFactory.create(ntDTO.getHost(), Integer.valueOf(ntDTO.getPort()), ntDTO.getUsername(), ntDTO.getPassword()));
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
                output.add(rRecorder);
            }
            return Result.success(output);
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
                Result<Set<Result<Terminal>>> rTerminals = rDto.flatMap(dto -> toTerminalSet(dto));
                Result<Set<Result<Recorder>>> rRecorders = rDto.flatMap(dto -> toRecorderSet(dto));
                Result<RoomFactory> rRoomFactory = Result.of(roomFactory, String.format("RoomFactory may not be null."));
                Result<Room> rRoom = rId.flatMap(id -> rName
                        .flatMap(name -> rTerminals
                                .flatMap(terminals -> rRecorders
                                        .flatMap(recorders -> rRoomFactory.map(roomFac -> roomFac.create(id, name, safeExtractSetResults(recorders), safeExtractSetResults(terminals)))))));
                output.add(rRoom);
            }
            return Result.success(output);
        });
    }

    public static <T> Set<T> safeExtractSetResults(Set<Result<T>> input) {
            Set<T> output = new HashSet<>();
            for (Result<T> elem : input) {
                elem.forEachOrFail(t -> output.add(t)).forEach(YamlDtoAssembler::logError);
            }
            return output;
    }
    public static <T> Set<T> safeExtractSetResults(Result<Set<Result<T>>> input) {

        Result<Set<T>> realOut = input.map(inputSet -> {
            Set<T> output = new HashSet<>();
            for (Result<T> elem : inputSet) {
                elem.forEachOrFail(t -> output.add(t)).forEach(YamlDtoAssembler::logError);
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
