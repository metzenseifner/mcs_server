package at.ac.uibk.mcsconnect.roomrepo.impl.unit;

import at.ac.uibk.mcsconnect.common.impl.NetworkTargetFactoryImpl;
import at.ac.uibk.mcsconnect.common.impl.integration.FakeNetworkTarget;
import at.ac.uibk.mcsconnect.executorservice.impl.integration.FakeMcsScheduledExecutorService;
import at.ac.uibk.mcsconnect.executorservice.impl.integration.FakeMcsSingletonExecutorService;
import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.recorderservice.api.Recorder;
import at.ac.uibk.mcsconnect.recorderservice.api.RecorderRunningStatesEnum;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import at.ac.uibk.mcsconnect.roomrepo.api.RoomRepo;
import at.ac.uibk.mcsconnect.roomrepo.api.Terminal;
import at.ac.uibk.mcsconnect.roomrepo.impl.RoomRepoInMemoryImpl;
import at.ac.uibk.mcsconnect.roomrepo.impl.integration.FakeRecorder;
import at.ac.uibk.mcsconnect.roomrepo.impl.integration.FakeRecorderFactory;
import at.ac.uibk.mcsconnect.roomrepo.impl.integration.FakeRoom;
import at.ac.uibk.mcsconnect.roomrepo.impl.integration.FakeRoomFactory;
import at.ac.uibk.mcsconnect.roomrepo.impl.integration.FakeTerminal;
import at.ac.uibk.mcsconnect.roomrepo.impl.integration.FakeTerminalFactory;
import at.ac.uibk.mcsconnect.sshsessionmanager.impl.integration.FakeSshSessionManagerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class RoomRepoInMemoryImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoomRepoInMemoryImplTest.class);

    RoomRepo roomRepo;

    @BeforeEach
    void setupFreshRoomService() {
        RoomRepo roomRepo = new RoomRepoInMemoryImpl(
                new FakeRoomFactory(),//RoomFactory roomFactory,
                new FakeRecorderFactory(), //RecorderFactory recorderFactory,
                new FakeTerminalFactory(), //TerminalFactory terminalFactory,
                new NetworkTargetFactoryImpl(), //NetworkTargetFactory networkTargetFactory,
                new FakeSshSessionManagerService(), //SshSessionManagerService sshSessionManagerService,
                new FakeMcsScheduledExecutorService(),//McsScheduledExecutorService mcsScheduledExecutorService,
                new FakeMcsSingletonExecutorService()//McsSingletonExecutorService mcsSingletonExecutorService
        );
        this.roomRepo = roomRepo;
    }

    // setup helpers
    static Terminal terminal = FakeTerminal.create("test01", "Test Machine", "192.168.0.2");
    static Set<Terminal> terminals = new HashSet<>();
    static { terminals.add(terminal); }
    static Set<Recorder> recorders = new HashSet<>();
    static {
        recorders.add(FakeRecorder.create("r", "main", FakeNetworkTarget.create("192.168.0.1", 22023, "test", "test"), RecorderRunningStatesEnum.STOPPED));
    }


    @Test
    @DisplayName("Init test")
    void initWorks() {
        Set<Room> rooms = roomRepo.getRooms();
        rooms.stream().forEach(r -> assertThat(r.hasRecorders()).isTrue());
        rooms.stream().forEach(r -> assertThat(r.hasTerminals()).isTrue());
    }

    @Test
    @DisplayName("Get existing room.")
    void canGetExistingRoomFromRepo() {
        Result<Room> rRoom = roomRepo.get("avstudio");
        assertThat(rRoom.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("Can retrieve localhost based on terminal host (ip | hostname)")
    void canGetRoomForLocalHost() {
        Result<Room> rRoom = roomRepo.getRoomForHost("127.0.0.1");
        rRoom.forEachOrFail(r -> assertThat(r.getId()).isEqualTo("avstudio")).forEach(RoomRepoInMemoryImplTest::logError);
        assertThat(rRoom.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("Test adding / getting new rooms")
    void canAddAndGetNewRoom() {
        Room room = FakeRoom.create("a_id", "a_name", recorders, terminals, Optional.empty());
        roomRepo.add(room);
        Result<Room> rRoom = roomRepo.getRoomForHost("192.168.0.2");
        rRoom.forEachOrFail(r -> assertThat(r.getId()).isEqualTo("a_id")).forEach(RoomRepoInMemoryImplTest::logError);
    }

    public static void logError(String msg) {
        LOGGER.error(msg);
    }
}
