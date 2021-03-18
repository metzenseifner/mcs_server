package at.ac.uibk.mcsconnect.roomrepo.impl.unit;


import at.ac.uibk.mcsconnect.common.impl.integration.FakeNetworkTargetFactory;
import at.ac.uibk.mcsconnect.executorservice.impl.integration.FakeMcsScheduledExecutorService;
import at.ac.uibk.mcsconnect.executorservice.impl.integration.FakeMcsSingletonExecutorService;
import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.roomrepo.api.Room;
import at.ac.uibk.mcsconnect.roomrepo.impl.hidden.YamlDtoAssembler;
import at.ac.uibk.mcsconnect.roomrepo.impl.hidden.yamldto.RoomDTO;
import at.ac.uibk.mcsconnect.roomrepo.impl.hidden.yamldto.RoomsDTO;
import at.ac.uibk.mcsconnect.roomrepo.impl.integration.FakeRecorderFactory;
import at.ac.uibk.mcsconnect.roomrepo.impl.integration.FakeRoomFactory;
import at.ac.uibk.mcsconnect.roomrepo.impl.integration.FakeTerminalFactory;
import at.ac.uibk.mcsconnect.sshsessionmanager.impl.integration.FakeSshSessionManagerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class RoomRepoYamlImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoomRepoYamlImplTest.class);
    private Path sampleRoomsYamlPath;
    private InputStream sampleRoomsInputStream;
    private YamlDtoAssembler yamlDtoAssembler;
    private Yaml yamlRoomsDTOParser;

    @BeforeEach
    void setup() {
        try {
            Path sampleYamlPath = Paths.get(getClass().getClassLoader().getResource("rooms.yml").toURI());
            this.sampleRoomsYamlPath = sampleYamlPath;
            InputStream sampleRoomsInputStream = new FileInputStream(sampleRoomsYamlPath.toFile());
            this.sampleRoomsInputStream = sampleRoomsInputStream;
        } catch (Exception e) {
            fail("Exception should not be thrown during setup: " + e);
        }
        Yaml yamlRoomsDTOParser = new Yaml(new Constructor(RoomsDTO.class)); // Handles instantiation recursively
        this.yamlDtoAssembler = new YamlDtoAssembler(
                new FakeRoomFactory(),
                new FakeRecorderFactory(),
                new FakeTerminalFactory(),
                new FakeNetworkTargetFactory(),
                new FakeSshSessionManagerService(),
                new FakeMcsScheduledExecutorService(),
                new FakeMcsSingletonExecutorService());
        this.yamlRoomsDTOParser = yamlRoomsDTOParser;
    }

    @Test
    void whenLoadYAMLDocumentWithTopLevelClass_thenLoadCorrectJavaObjectWithNestedObjects() {
        try {
            InputStream inputStream = new FileInputStream(sampleRoomsYamlPath.toFile());
            RoomsDTO roomsDTO = yamlRoomsDTOParser.load(inputStream);
            assertThat(roomsDTO.getRooms().size()).isEqualTo(2);
            RoomDTO avstudioDTO = roomsDTO.getRooms().get("avstudio");
            assertThat(avstudioDTO.getName()).isEqualTo("AV Studio");
            RoomDTO hs01DTO = roomsDTO.getRooms().get("hs01");
            //assertThat(hs01DTO.successValue().getName()).isNull();
        } catch (FileNotFoundException f) {
            logError(f.getMessage());
        }
    }

    @Test
    @DisplayName("Tests the toTerminalSet function")
    void canMapTerminalDTOToTerminal() {
        try {
            RoomsDTO roomsDTO = yamlRoomsDTOParser.load(sampleRoomsInputStream);
            RoomDTO avstudioDTO = roomsDTO.getRooms().get("avstudio");
            yamlDtoAssembler.toTerminalSet(avstudioDTO)
                    .forEachOrFail(set -> assertThat(set.size()).isEqualTo(2))
                    .forEach(RoomRepoYamlImplTest::logError);
            //assertThat(terminals.toString()).isEqualTo("[FakeTerminal(t_nm-pc12), FakeTerminal(t_nm-pc11)]");
        } catch (Exception e) {
            logError(e.getMessage());
        }
    }

    @Test
    @DisplayName("Tests the toRecorderSet function")
    void canMapRecorderDTOToRecorder() {
        try {
            RoomsDTO roomsDTO = yamlRoomsDTOParser.load(sampleRoomsInputStream);
            RoomDTO avstudioDTO = roomsDTO.getRooms().get("avstudio");
            yamlDtoAssembler.toRecorderSet(avstudioDTO)
                    .forEachOrFail(set -> assertThat(set.size()).isEqualTo(2))
                    .forEach(RoomRepoYamlImplTest::logError);
        } catch (Exception e) {
            logError(e.getMessage());
        }
    }

    @Test
    @DisplayName("Tests the toRoomSet function")
    void canMapRoomDTOToRoom() {
        try {
            RoomsDTO roomsDTO = yamlRoomsDTOParser.load(sampleRoomsInputStream);
            Set<Room> rooms = yamlDtoAssembler.toRoomSet(roomsDTO);
            Optional<Room> oRoom = rooms.stream().filter(r -> r.getId().equals("avstudio")).findFirst();
            assertThat(oRoom.isPresent()).isTrue();

            Room sut = oRoom.get();


            assertThat(sut.getRecorders().size()).isEqualTo(2);
            assertThat(sut.getTerminals().size()).isEqualTo(2);

        } catch (Exception e) {
            logError(e.getMessage());
        }
    }

    private Yaml setupSampleParser() {
        return yamlRoomsDTOParser;
    }
    private static void logInfo(String msg) {
        LOGGER.info(msg);
    }
    private static void logError(String msg) {
        fail(msg);
    }
}
